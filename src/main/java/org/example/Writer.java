package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Writer {

    public static void writeOutput(String fileName, List<HeapEntry> heapEntries, boolean includeNullCpa) {
        Logger.info("Writing " + fileName);

        try (var writer = Files.newBufferedWriter(Path.of(fileName), StandardCharsets.UTF_8)) {

            writer.append("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA\n");

            for (HeapEntry entry : heapEntries) {
                CampaignStats stats = entry.stats();
                Double cpa = stats.cpa();

                if (!includeNullCpa && cpa == null) {
                    continue;
                }

                writer.append(entry.campaignId()).append(",");
                writer.append(String.valueOf(stats.getImpressions())).append(",");
                writer.append(String.valueOf(stats.getClicks())).append(",");
                writer.append(String.format("%.2f", stats.getSpend())).append(",");
                writer.append(String.valueOf(stats.getConversions())).append(",");
                writer.append(String.format("%.4f", stats.ctr())).append(",");
                writer.append(cpa == null ? "null" : String.format("%.2f", cpa));
                writer.append("\n");
            }
        } catch (IOException e) {
            Logger.error("Error writing file: " + e.getMessage());
        }
    }
}
