package org.example;

public class ProgressBar {
    private final long total;
    private final String desc;
    private long current;
    private long lastUpdate;
    private static final long UPDATE_INTERVAL = 100000;

    public ProgressBar(long total, String desc) {
        this.total = total;
        this.desc = desc;
        this.current = 0;
        this.lastUpdate = 0;
    }

    public void update() {
        current++;
        if (current - lastUpdate >= UPDATE_INTERVAL) {
            display();
            lastUpdate = current;
        }
    }

    private void display() {
        if (total > 0) {
            double percent = (double) current / total * 100;
            int barLength = 40;
            int filled = (int) (barLength * current / total);
            String bar = "█".repeat(filled) + "░".repeat(barLength - filled); // Unicode bar characters
            System.out.print("\r" + desc + ": |" + bar + "| " + String.format("%.1f", percent) + "% (" +
                String.format("%,d", current) + "/" + String.format("%,d", total) + ")");
        } else {
            System.out.print("\r" + desc + ": " + String.format("%,d", current) + " rows");
        }
        System.out.flush();
    }

    public void close() {
        display();
        System.out.println();
        System.out.flush();
    }

    public long getCurrent() {
        return current;
    }
}
