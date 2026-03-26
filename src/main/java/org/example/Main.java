package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            Logger.info("Usage: java -jar csv-processor.jar <input-csv-file>");
            System.exit(1);
        }

        String filePath = args[0];
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            Logger.info("Error: File does not exist: " + filePath);
            System.exit(1);
        }

        if (!Files.isRegularFile(path)) {
            Logger.info("Error: Path is not a regular file: " + filePath);
            System.exit(1);
        }


        String fileName = path.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".csv")) {
            Logger.info("Error: File must be a CSV file with .csv extension: " + filePath);
            System.exit(1);
        }

        if (!Files.isReadable(path)) {
            Logger.info("Error: File is not readable: " + filePath);
            System.exit(1);
        }

        Processor.processFile(filePath);
    }
}