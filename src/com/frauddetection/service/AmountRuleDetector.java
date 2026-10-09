package com.frauddetection.service;

import com.frauddetection.model.Transaction;

public class AmountRuleDetector implements FraudDetector {
    private final FraudDetectionConfig config;

    public AmountRuleDetector(double defaultThreshold) {
        this(FraudDetectionConfig.getInstance());
    }

    public AmountRuleDetector(FraudDetectionConfig config) {
        this.config = config != null ? config : FraudDetectionConfig.getInstance();
    }

    public double getThreshold() {
        return config.getAmountThreshold();
    }

    public void setThreshold(double threshold) {
        config.setAmountThreshold(threshold);
    }

    @Override
    public String check(Transaction txn) {
        double currentThreshold = getThreshold();
        if (txn.getAmount() > currentThreshold) {
            return "Amount " + txn.getAmount() + " exceeds threshold of " + currentThreshold;
        }
        return null;
    }

    @Override
    public String getName() { return "Amount Rule"; }
}
