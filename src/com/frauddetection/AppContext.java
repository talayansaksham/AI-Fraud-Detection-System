package com.frauddetection;

import com.frauddetection.dao.AlertDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.dao.UserDAO;
import com.frauddetection.service.AmountRuleDetector;
import com.frauddetection.service.FraudDetectionConfig;
import com.frauddetection.service.FraudDetectionService;
import com.frauddetection.service.LocationRuleDetector;
import com.frauddetection.service.MLClassifierDetector;
import com.frauddetection.service.VelocityRuleDetector;

public class AppContext {
    private static volatile AppContext instance;
    private static final Object INIT_LOCK = new Object();

    // Shared active singleton components across the application
    private static UserDAO sharedUserDAO;
    private static TransactionDAO sharedTransactionDAO;
    private static AlertDAO sharedAlertDAO;
    private static FraudDetectionConfig sharedConfig;
    private static FraudDetectionService sharedDetectionService;
    private static AmountRuleDetector sharedAmountRule;
    private static VelocityRuleDetector sharedVelocityRule;
    private static LocationRuleDetector sharedLocationRule;
    private static MLClassifierDetector sharedMLClassifier;

    public final UserDAO userDAO;
    public final TransactionDAO transactionDAO;
    public final AlertDAO alertDAO;
    public final FraudDetectionService detectionService;
    public final FraudDetectionConfig config;

    // Exposed so Config / Algorithm panels can read and update parameters at runtime
    public final AmountRuleDetector amountRule;
    public final VelocityRuleDetector velocityRule;
    public final LocationRuleDetector locationRule;
    public final MLClassifierDetector mlClassifier;

    public static AppContext getInstance() {
        if (instance == null) {
            synchronized (INIT_LOCK) {
                if (instance == null) {
                    instance = new AppContext();
                }
            }
        }
        return instance;
    }

    public AppContext() {
        synchronized (INIT_LOCK) {
            if (sharedConfig == null) {
                sharedConfig = FraudDetectionConfig.getInstance();
                sharedUserDAO = new UserDAO();
                sharedTransactionDAO = new TransactionDAO();
                sharedAlertDAO = new AlertDAO();

                sharedDetectionService = new FraudDetectionService(sharedTransactionDAO, sharedAlertDAO, sharedConfig);

                sharedAmountRule = new AmountRuleDetector(sharedConfig);
                sharedVelocityRule = new VelocityRuleDetector(sharedTransactionDAO, sharedConfig);
                sharedLocationRule = new LocationRuleDetector(sharedUserDAO);
                sharedMLClassifier = new MLClassifierDetector("training_data.arff");

                sharedDetectionService.addDetector(sharedAmountRule);
                sharedDetectionService.addDetector(sharedVelocityRule);
                sharedDetectionService.addDetector(sharedLocationRule);
                sharedDetectionService.addDetector(sharedMLClassifier);
            }

            this.config = sharedConfig;
            this.userDAO = sharedUserDAO;
            this.transactionDAO = sharedTransactionDAO;
            this.alertDAO = sharedAlertDAO;
            this.detectionService = sharedDetectionService;
            this.amountRule = sharedAmountRule;
            this.velocityRule = sharedVelocityRule;
            this.locationRule = sharedLocationRule;
            this.mlClassifier = sharedMLClassifier;

            if (instance == null) {
                instance = this;
            }
        }
    }
}
