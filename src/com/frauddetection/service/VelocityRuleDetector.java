package com.frauddetection.service;

import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.model.Transaction;
import java.sql.SQLException;

public class VelocityRuleDetector implements FraudDetector {
    private final TransactionDAO transactionDAO;
    private final FraudDetectionConfig config;

    public VelocityRuleDetector(TransactionDAO transactionDAO, int maxTransactions, int windowMinutes) {
        this(transactionDAO, FraudDetectionConfig.getInstance());
    }

    public VelocityRuleDetector(TransactionDAO transactionDAO, FraudDetectionConfig config) {
        this.transactionDAO = transactionDAO;
        this.config = config != null ? config : FraudDetectionConfig.getInstance();
    }

    public int getMaxTransactions() {
        return config.getVelocityMaxTransactions();
    }

    public void setMaxTransactions(int maxTransactions) {
        config.setVelocityMaxTransactions(maxTransactions);
    }

    public int getWindowMinutes() {
        return config.getVelocityWindowMinutes();
    }

    public void setWindowMinutes(int windowMinutes) {
        config.setVelocityWindowMinutes(windowMinutes);
    }

    @Override
    public String check(Transaction txn) {
        try {
            int window = getWindowMinutes();
            int max = getMaxTransactions();
            int recentCount = transactionDAO.countRecentTransactionsByUser(txn.getUserId(), window);
            if (recentCount > max) {
                return "User " + txn.getUserId() + " made " + recentCount
                        + " transactions in the last " + window + " minute(s)";
            }
        } catch (SQLException e) {
            System.err.println("VelocityRuleDetector could not query DB: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getName() { return "Velocity Rule"; }
}
