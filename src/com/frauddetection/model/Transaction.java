package com.frauddetection.model;

import java.time.LocalDateTime;

public class Transaction {

    private int txnId;
    private int userId;
    private double amount;
    private String location;
    private String merchantCategory;
    private LocalDateTime timestamp;
    private String status;

    public Transaction(int userId, double amount, String location, String merchantCategory) {
        this.userId = userId;
        this.amount = amount;
        this.location = location;
        this.merchantCategory = merchantCategory;
        this.timestamp = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Transaction(int txnId, int userId, double amount, String location,
                        String merchantCategory, LocalDateTime timestamp, String status) {
        this.txnId = txnId;
        this.userId = userId;
        this.amount = amount;
        this.location = location;
        this.merchantCategory = merchantCategory;
        this.timestamp = timestamp;
        this.status = status;
    }

    public int getTxnId() { return txnId; }
    public void setTxnId(int txnId) { this.txnId = txnId; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
    public String getLocation() { return location; }
    public String getMerchantCategory() { return merchantCategory; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Txn#%d | User %d | %.2f | %s | %s | %s",
                txnId, userId, amount, location, merchantCategory, status);
    }
}
