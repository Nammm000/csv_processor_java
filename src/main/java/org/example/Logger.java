package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";

    public static void info(String message) {
        System.out.println();
        System.out.println(ANSI_GREEN + "[" + LocalDateTime.now().format(FORMATTER) + "] [INFO] " + ANSI_RESET + message);
    }

    public static void warn(String message) {
        System.out.println();
        System.out.println(ANSI_YELLOW + "[" + LocalDateTime.now().format(FORMATTER) + "] [WARN] " + ANSI_RESET + message);
    }

    public static void error(String message) {
        System.out.println();
        System.out.println(ANSI_RED + "[" + LocalDateTime.now().format(FORMATTER) + "] [ERROR] " + ANSI_RESET + message);
    }

    public static void logMemory() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memMB = usedMemory / (1024.0 * 1024.0);
        info(String.format("Memory usage: %.2f MB", memMB));
    }
}
