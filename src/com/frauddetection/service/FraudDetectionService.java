package com.frauddetection.service;

import com.frauddetection.dao.AlertDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.model.Alert;
import com.frauddetection.model.Transaction;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FraudDetectionService {
    private final List<FraudDetector> detectors = new ArrayList<>();
    private final FraudDetectionConfig config;
    private final TransactionDAO transactionDAO;
    private final AlertDAO alertDAO;

    public FraudDetectionService(TransactionDAO transactionDAO, AlertDAO alertDAO) {
        this(transactionDAO, alertDAO, FraudDetectionConfig.getInstance());
    }

    public FraudDetectionService(TransactionDAO transactionDAO, AlertDAO alertDAO, FraudDetectionConfig config) {
        this.transactionDAO = transactionDAO;
        this.alertDAO = alertDAO;
        this.config = config != null ? config : FraudDetectionConfig.getInstance();
    }

    public void addDetector(FraudDetector detector) { detectors.add(detector); }
    public List<FraudDetector> getDetectors() { return detectors; }
    public FraudDetectionConfig getConfig() { return config; }

    public boolean isDetectorEnabled(String name) {
        return config.isDetectorEnabled(name);
    }

    public void setDetectorEnabled(String name, boolean enabled) {
        config.setDetectorEnabled(name, enabled);
    }

    public DetectionResult evaluate(Transaction txn) throws SQLException {
        config.reloadIfModified();
        DetectionResult result = new DetectionResult();
        for (FraudDetector detector : detectors) {
            if (!config.isDetectorEnabled(detector.getName())) continue;
            String reason = detector.check(txn);
            if (reason != null) result.addReason(detector.getName(), reason);
        }

        if (result.isFlagged()) {
            transactionDAO.updateStatus(txn.getTxnId(), "FLAGGED");
            Alert alert = new Alert(txn.getTxnId(), result.getRiskLevel(), result.getReasonSummary());
            alertDAO.addAlert(alert);
        } else {
            transactionDAO.updateStatus(txn.getTxnId(), "APPROVED");
        }
        return result;
    }
}
