package com.frauddetection.service;

import java.io.*;
import java.util.*;

/**
 * Shared active configuration for the Fraud Detection System.
 *
 * Acts as the single source of truth for runtime fraud detection rules:
 * - Amount Threshold
 * - Velocity Maximum Transactions
 * - Velocity Time Window (minutes)
 * - Algorithm Enabled / Disabled States
 *
 * Backed by both thread-safe in-memory state and a persistent properties file
 * ("fraud_detection_config.properties") so that configuration changes applied
 * by the Admin Portal are instantly visible to the Customer Portal whether both
 * portals run in the same JVM process or across separate JVM processes.
 */
public class FraudDetectionConfig {

    public static final String CONFIG_FILE_NAME = "fraud_detection_config.properties";
    private static volatile FraudDetectionConfig instance;
    private static final Object LOCK = new Object();

    private double amountThreshold = 5000.0;
    private int velocityMaxTransactions = 3;
    private int velocityWindowMinutes = 5;
    private final Set<String> disabledDetectors = Collections.synchronizedSet(new HashSet<>());

    private final File configFile;
    private long lastFileModified = 0;

    public static FraudDetectionConfig getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new FraudDetectionConfig();
                }
            }
        }
        return instance;
    }

    public FraudDetectionConfig() {
        this(new File(CONFIG_FILE_NAME));
    }

    public FraudDetectionConfig(File configFile) {
        this.configFile = configFile;
        load();
    }

    /**
     * Loads the configuration from the backing properties file.
     * If the file does not exist, writes the current defaults to create it.
     */
    public synchronized void load() {
        if (!configFile.exists()) {
            save();
            return;
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configFile)) {
            props.load(in);

            String amountStr = props.getProperty("amount.threshold");
            if (amountStr != null) {
                this.amountThreshold = Double.parseDouble(amountStr.trim());
            }

            String velocityCountStr = props.getProperty("velocity.max_transactions");
            if (velocityCountStr != null) {
                this.velocityMaxTransactions = Integer.parseInt(velocityCountStr.trim());
            }

            String velocityWindowStr = props.getProperty("velocity.window_minutes");
            if (velocityWindowStr != null) {
                this.velocityWindowMinutes = Integer.parseInt(velocityWindowStr.trim());
            }

            disabledDetectors.clear();
            String disabledStr = props.getProperty("disabled.detectors");
            if (disabledStr != null && !disabledStr.trim().isEmpty()) {
                String[] parts = disabledStr.split(";;");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        disabledDetectors.add(trimmed);
                    }
                }
            }

            lastFileModified = configFile.lastModified();
        } catch (Exception e) {
            System.err.println("FraudDetectionConfig: Error reading " + configFile.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Persists the active configuration to the backing properties file.
     */
    public synchronized void save() {
        Properties props = new Properties();
        props.setProperty("amount.threshold", String.valueOf(amountThreshold));
        props.setProperty("velocity.max_transactions", String.valueOf(velocityMaxTransactions));
        props.setProperty("velocity.window_minutes", String.valueOf(velocityWindowMinutes));
        props.setProperty("disabled.detectors", String.join(";;", disabledDetectors));

        try (OutputStream out = new FileOutputStream(configFile)) {
            props.store(out, "Fraud Detection System — Shared Active Configuration");
            lastFileModified = configFile.lastModified();
        } catch (IOException e) {
            System.err.println("FraudDetectionConfig: Error saving " + configFile.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Checks if the backing properties file was modified externally (e.g., by another JVM process)
     * and reloads it if necessary.
     */
    public synchronized void reloadIfModified() {
        if (configFile.exists() && configFile.lastModified() > lastFileModified) {
            load();
        }
    }

    public synchronized double getAmountThreshold() {
        reloadIfModified();
        return amountThreshold;
    }

    public synchronized void setAmountThreshold(double amountThreshold) {
        this.amountThreshold = amountThreshold;
        save();
    }

    public synchronized int getVelocityMaxTransactions() {
        reloadIfModified();
        return velocityMaxTransactions;
    }

    public synchronized void setVelocityMaxTransactions(int velocityMaxTransactions) {
        this.velocityMaxTransactions = velocityMaxTransactions;
        save();
    }

    public synchronized int getVelocityWindowMinutes() {
        reloadIfModified();
        return velocityWindowMinutes;
    }

    public synchronized void setVelocityWindowMinutes(int velocityWindowMinutes) {
        this.velocityWindowMinutes = velocityWindowMinutes;
        save();
    }

    public synchronized boolean isDetectorEnabled(String detectorName) {
        reloadIfModified();
        return !disabledDetectors.contains(detectorName);
    }

    public synchronized void setDetectorEnabled(String detectorName, boolean enabled) {
        reloadIfModified();
        if (enabled) {
            disabledDetectors.remove(detectorName);
        } else {
            disabledDetectors.add(detectorName);
        }
        save();
    }

    public synchronized Set<String> getDisabledDetectors() {
        reloadIfModified();
        return new HashSet<>(disabledDetectors);
    }
}
