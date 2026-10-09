package com.frauddetection.service;

import java.util.ArrayList;
import java.util.List;

public class DetectionResult {
    private final List<String> reasons = new ArrayList<>();

    public void addReason(String detectorName, String reason) {
        reasons.add("[" + detectorName + "] " + reason);
    }

    public boolean isFlagged() { return !reasons.isEmpty(); }
    public List<String> getReasons() { return reasons; }

    public String getRiskLevel() {
        int count = reasons.size();
        if (count == 0) return "NONE";
        if (count == 1) return "LOW";
        if (count == 2) return "MEDIUM";
        return "HIGH";
    }

    public int getRiskScore() {
        int count = reasons.size();
        if (count == 0) return 5;
        if (count == 1) return 50;
        if (count == 2) return 78;
        return 95;
    }

    public static int scoreForLevel(String level) {
        if (level == null) return 5;
        switch (level.toUpperCase()) {
            case "LOW": return 50;
            case "MEDIUM": return 78;
            case "HIGH": return 95;
            default: return 5;
        }
    }

    public String getReasonSummary() { return String.join("; ", reasons); }
}
