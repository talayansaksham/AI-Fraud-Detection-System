package com.frauddetection.model;

import java.time.LocalDateTime;

public class Alert {

    private int alertId;
    private int txnId;
    private String riskLevel;
    private String reason;
    private LocalDateTime createdAt;
    private String transactionStatus;

    public Alert(int txnId, String riskLevel, String reason) {
        this.txnId = txnId;
        this.riskLevel = riskLevel;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public Alert(int alertId, int txnId, String riskLevel, String reason, LocalDateTime createdAt) {
        this.alertId = alertId;
        this.txnId = txnId;
        this.riskLevel = riskLevel;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }
    public int getTxnId() { return txnId; }
    public String getRiskLevel() { return riskLevel; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }

    @Override
    public String toString() {
        return String.format("Alert on Txn#%d | %s | %s", txnId, riskLevel, reason);
    }
}
