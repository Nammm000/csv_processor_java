package org.example;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Processor {

    public static void processFile(String filePath) throws NumberFormatException {
        Logger.info("Starting processing: " + filePath);

        Object2LongOpenHashMap<String> impressionsMap = new Object2LongOpenHashMap<>();
        Object2LongOpenHashMap<String> clicksMap = new Object2LongOpenHashMap<>();
        Object2DoubleOpenHashMap<String> spendMap = new Object2DoubleOpenHashMap<>();
        Object2LongOpenHashMap<String> conversionsMap = new Object2LongOpenHashMap<>();

        Set<String> campaignIds = new HashSet<>();

        Logger.info("Counting rows for progress bar...");
        long totalRows = 0;
        try {
            totalRows = countRowsFast(filePath);
        } catch (IOException e) {
            Logger.error("Error counting rows: " + e.getMessage());
            return;
        }

        Logger.info(String.format("Found %,d rows. Starting processing...", totalRows));

        ProgressBar progress = new ProgressBar(totalRows, "Processing rows");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                 .builder()
                 .setHeader()
                 .setSkipHeaderRecord(true)
                 .build())) {

            for (CSVRecord record : csvParser) {
                String campaignId = record.get("campaign_id");

                try {
                    long impressions = Long.parseLong(record.get("impressions"));
                    long clicks = Long.parseLong(record.get("clicks"));
                    double spend = Double.parseDouble(record.get("spend"));
                    long conversions = Long.parseLong(record.get("conversions"));

                    campaignIds.add(campaignId);
                    impressionsMap.put(campaignId, impressionsMap.getOrDefault(campaignId, 0L) + impressions);
                    clicksMap.put(campaignId, clicksMap.getOrDefault(campaignId, 0L) + clicks);
                    spendMap.put(campaignId, spendMap.getOrDefault(campaignId, 0.0) + spend);
                    conversionsMap.put(campaignId, conversionsMap.getOrDefault(campaignId, 0L) + conversions);

                } catch (NumberFormatException e) {
                    Logger.error("Error parsing number at campaign_id " + campaignId + ": " + e.getMessage());
                    return;
                }

                progress.update();

                if (progress.getCurrent() % 1_000_000 == 0) {
                    Logger.info(String.format("Processed %,d rows", progress.getCurrent()));
                    Logger.logMemory();
                }
            }
        } catch (IOException e) {
            Logger.error("Error reading file: " + e.getMessage());
            return;
        }

        progress.close();

        Logger.info(String.format("Finished reading %,d rows", progress.getCurrent()));
        Logger.logMemory();

        generateTopHighestCtr(campaignIds, impressionsMap, clicksMap, spendMap, conversionsMap);
        generateTopLowestCpa(campaignIds, impressionsMap, clicksMap, spendMap, conversionsMap);

        Logger.info("Processing completed successfully");
    }

    public static long countRowsFast(String filePath) throws IOException {
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(filePath))) {
            byte[] buffer = new byte[8192];
            long count = 0;
            int read;

            while ((read = is.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    if (buffer[i] == '\n') count++;
                }
            }
            return count;
        }
    }

    private static void generateTopHighestCtr(Set<String> campaignIds,
                                              Object2LongOpenHashMap<String> impressionsMap,
                                              Object2LongOpenHashMap<String> clicksMap,
                                              Object2DoubleOpenHashMap<String> spendMap,
                                              Object2LongOpenHashMap<String> conversionsMap) {
        Logger.info("Generating top 10 highest CTR");

        PriorityQueue<HeapEntry> heap = new PriorityQueue<>(
            Comparator.comparingDouble(HeapEntry::metric)
        );

        for (String campaignId : campaignIds) {
            long impressions = impressionsMap.getOrDefault(campaignId, 0L);
            long clicks = clicksMap.getOrDefault(campaignId, 0L);
            double spend = spendMap.getOrDefault(campaignId, 0.0);
            long conversions = conversionsMap.getOrDefault(campaignId, 0L);

            CampaignStats stats = new CampaignStats(impressions, clicks, spend, conversions);
            double ctrValue = stats.ctr();

            heap.offer(new HeapEntry(ctrValue, campaignId, stats));

            if (heap.size() > 10) {
                heap.poll();
            }
        }

        List<HeapEntry> top = new ArrayList<>(heap);
        top.sort(Comparator.comparingDouble(HeapEntry::metric).reversed());

        Writer.writeOutput("top10_ctr.csv", top, true);
    }

    private static void generateTopLowestCpa(Set<String> campaignIds,
                                             Object2LongOpenHashMap<String> impressionsMap,
                                             Object2LongOpenHashMap<String> clicksMap,
                                             Object2DoubleOpenHashMap<String> spendMap,
                                             Object2LongOpenHashMap<String> conversionsMap) {
        Logger.info("Generating top 10 lowest CPA");

        PriorityQueue<HeapEntry> heap = new PriorityQueue<>(
            Comparator.comparingDouble(HeapEntry::metric)
        );

        for (String campaignId : campaignIds) {
            long impressions = impressionsMap.getOrDefault(campaignId, 0L);
            long clicks = clicksMap.getOrDefault(campaignId, 0L);
            double spend = spendMap.getOrDefault(campaignId, 0.0);
            long conversions = conversionsMap.getOrDefault(campaignId, 0L);

            CampaignStats stats = new CampaignStats(impressions, clicks, spend, conversions);
            Double cpaValue = stats.cpa();

            if (cpaValue == null) {
                continue;
            }

            heap.offer(new HeapEntry(-cpaValue, campaignId, stats));

            if (heap.size() > 10) {
                heap.poll();
            }
        }

        List<HeapEntry> top = new ArrayList<>(heap);
        top.sort(Comparator.comparingDouble(HeapEntry::metric).reversed());

        List<HeapEntry> result = new ArrayList<>();
        for (HeapEntry entry : top) {
            result.add(new HeapEntry(-entry.metric(), entry.campaignId(), entry.stats()));
        }

        Writer.writeOutput("top10_cpa.csv", result, false);
    }
}
