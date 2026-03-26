package org.example;
public class CampaignStats {
    private long impressions;
    private long clicks;
    private double spend;
    private long conversions;

    public CampaignStats(long impressions, long clicks, double spend, long conversions) {
        this.impressions = impressions;
        this.clicks = clicks;
        this.spend = spend;
        this.conversions = conversions;
    }

    public double ctr() {
        return impressions == 0 ? 0.0 : (double) clicks / impressions;
    }

    public Double cpa() {
        return conversions == 0 ? null : spend / conversions;
    }

    public long getImpressions() {
        return impressions;
    }

    public long getClicks() {
        return clicks;
    }

    public double getSpend() {
        return spend;
    }

    public long getConversions() {
        return conversions;
    }
}
