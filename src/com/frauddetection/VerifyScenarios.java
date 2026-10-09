package com.frauddetection;

import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;
import com.frauddetection.service.DetectionResult;
import com.frauddetection.service.FraudDetectionConfig;

import java.util.List;

public class VerifyScenarios {

    private static int passed = 0;
    private static int failed = 0;

    private static void assertTrue(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("  [PASS] " + testName + " - " + details);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName + " - " + details);
            failed++;
        }
    }

    private static boolean hasReason(DetectionResult res, String detectorName) {
        return res.getReasons().stream().anyMatch(r -> r.contains("[" + detectorName + "]"));
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("STARTING FRAUD DETECTION SYSTEM INTEGRATION TESTS");
        System.out.println("=================================================\n");

        AppContext adminCtx = AppContext.getInstance();
        User testUser = null;
        try {
            List<User> users = adminCtx.userDAO.getAllUsers();
            if (users.isEmpty()) {
                System.err.println("Error: No users found in database.");
                return;
            }
            testUser = users.get(0); // Rahul (Delhi)
            System.out.println("Using test user: " + testUser.getUsername() + " (ID: " + testUser.getUserId() + ", Usual Location: " + testUser.getUsualLocation() + ")\n");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // =========================================================
        // SCENARIO 1, 2, 3: AMOUNT THRESHOLD DYNAMIC CONFIGURATION
        // =========================================================
        System.out.println("--- Testing Scenario 1, 2, 3: Amount Threshold (5000 -> 10000) ---");

        // Step 1: Admin sets threshold to 5000
        adminCtx.amountRule.setThreshold(5000.0);
        assertTrue("Setup Amount = 5000", adminCtx.amountRule.getThreshold() == 5000.0,
                "Admin set threshold to " + adminCtx.amountRule.getThreshold());

        // Customer transaction of 7000
        Transaction txn1 = new Transaction(testUser.getUserId(), 7000.0, testUser.getUsualLocation(), "Groceries");
        try {
            int id1 = adminCtx.transactionDAO.addTransaction(txn1);
            txn1.setTxnId(id1);
            DetectionResult res1 = adminCtx.detectionService.evaluate(txn1);

            boolean flaggedByAmount = hasReason(res1, "Amount Rule");
            assertTrue("Test 1: Amount 7000 with Threshold 5000",
                    flaggedByAmount,
                    "Result is flagged: " + res1.isFlagged() + ", Reasons: " + res1.getReasonSummary());
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        // Step 2: Admin changes threshold to 10000
        adminCtx.amountRule.setThreshold(10000.0);
        assertTrue("Admin Changes Amount = 10000", adminCtx.amountRule.getThreshold() == 10000.0,
                "Admin updated threshold to " + adminCtx.amountRule.getThreshold());

        // Customer makes transaction of 7000 again
        Transaction txn2 = new Transaction(testUser.getUserId(), 7000.0, testUser.getUsualLocation(), "Groceries");
        try {
            int id2 = adminCtx.transactionDAO.addTransaction(txn2);
            txn2.setTxnId(id2);
            DetectionResult res2 = adminCtx.detectionService.evaluate(txn2);

            boolean flaggedByAmount = hasReason(res2, "Amount Rule");
            assertTrue("Test 2: Amount 7000 with Threshold 10000",
                    !flaggedByAmount,
                    "Amount Rule did NOT flag 7000 < 10000. Flagged: " + flaggedByAmount + ", Reasons: " + res2.getReasonSummary());
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        // Step 3: Customer makes transaction of 11000
        Transaction txn3 = new Transaction(testUser.getUserId(), 11000.0, testUser.getUsualLocation(), "Electronics");
        try {
            int id3 = adminCtx.transactionDAO.addTransaction(txn3);
            txn3.setTxnId(id3);
            DetectionResult res3 = adminCtx.detectionService.evaluate(txn3);

            boolean flaggedByAmount = hasReason(res3, "Amount Rule");
            assertTrue("Test 3: Amount 11000 with Threshold 10000",
                    flaggedByAmount,
                    "Amount Rule flagged 11000 > 10000. Flagged: " + flaggedByAmount + ", Reasons: " + res3.getReasonSummary());
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        // =========================================================
        // SCENARIO 4: VELOCITY THRESHOLD DYNAMIC CONFIGURATION
        // =========================================================
        System.out.println("\n--- Testing Scenario 4: Velocity Limit (3 -> 5 in 5 mins) ---");

        // Create a temporary new user for clean velocity test
        User velocityUser = null;
        try {
            String uname = "vel_test_" + System.currentTimeMillis();
            velocityUser = new User(0, uname, uname + "@test.com", "Delhi", "pass");
            int uid = adminCtx.userDAO.addUser(velocityUser);
            velocityUser.setUserId(uid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set Velocity Limit = 3, Window = 5
        adminCtx.velocityRule.setMaxTransactions(3);
        adminCtx.velocityRule.setWindowMinutes(5);
        assertTrue("Setup Velocity = 3, Window = 5",
                adminCtx.velocityRule.getMaxTransactions() == 3 && adminCtx.velocityRule.getWindowMinutes() == 5,
                "Max: " + adminCtx.velocityRule.getMaxTransactions() + ", Window: " + adminCtx.velocityRule.getWindowMinutes());

        try {
            // First 3 transactions (amounts low, location usual)
            for (int i = 1; i <= 3; i++) {
                Transaction vt = new Transaction(velocityUser.getUserId(), 100.0, "Delhi", "Dining");
                int vtid = adminCtx.transactionDAO.addTransaction(vt);
                vt.setTxnId(vtid);
                DetectionResult vr = adminCtx.detectionService.evaluate(vt);
                assertTrue("Velocity Txn " + i + "/3", !hasReason(vr, "Velocity Rule"),
                        "Txn " + i + " not flagged by Velocity Rule. Total reasons: " + vr.getReasons().size());
            }

            // 4th transaction (exceeds limit 3)
            Transaction vt4 = new Transaction(velocityUser.getUserId(), 100.0, "Delhi", "Dining");
            int vtid4 = adminCtx.transactionDAO.addTransaction(vt4);
            vt4.setTxnId(vtid4);
            DetectionResult vr4 = adminCtx.detectionService.evaluate(vt4);
            assertTrue("Velocity Txn 4/3 exceeds limit 3", hasReason(vr4, "Velocity Rule"),
                    "Velocity Rule flagged: " + vr4.getReasonSummary());

            // Admin changes Velocity Limit to 5
            adminCtx.velocityRule.setMaxTransactions(5);
            assertTrue("Admin changes Velocity Limit to 5", adminCtx.velocityRule.getMaxTransactions() == 5,
                    "New Max: " + adminCtx.velocityRule.getMaxTransactions());

            // 5th transaction (count = 5, not exceeding limit 5)
            Transaction vt5 = new Transaction(velocityUser.getUserId(), 100.0, "Delhi", "Dining");
            int vtid5 = adminCtx.transactionDAO.addTransaction(vt5);
            vt5.setTxnId(vtid5);
            DetectionResult vr5 = adminCtx.detectionService.evaluate(vt5);
            assertTrue("Velocity Txn 5/5 with new limit 5", !hasReason(vr5, "Velocity Rule"),
                    "Txn 5 is within limit 5. Flagged by Velocity: " + hasReason(vr5, "Velocity Rule"));

            // 6th transaction (count = 6, exceeds limit 5)
            Transaction vt6 = new Transaction(velocityUser.getUserId(), 100.0, "Delhi", "Dining");
            int vtid6 = adminCtx.transactionDAO.addTransaction(vt6);
            vt6.setTxnId(vtid6);
            DetectionResult vr6 = adminCtx.detectionService.evaluate(vt6);
            assertTrue("Velocity Txn 6/5 exceeds new limit 5", hasReason(vr6, "Velocity Rule"),
                    "Txn 6 flagged by Velocity Rule: " + vr6.getReasonSummary());

        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        // =========================================================
        // SCENARIO 5: ALGORITHM MANAGEMENT TOGGLES
        // =========================================================
        System.out.println("\n--- Testing Scenario 5: Algorithm Management Toggles ---");

        // 5.1 Amount Rule Toggle
        try {
            adminCtx.amountRule.setThreshold(5000.0);
            adminCtx.detectionService.setDetectorEnabled("Amount Rule", false);
            assertTrue("Disable Amount Rule", !adminCtx.detectionService.isDetectorEnabled("Amount Rule"), "Amount Rule disabled");

            Transaction tAmtDisabled = new Transaction(testUser.getUserId(), 20000.0, testUser.getUsualLocation(), "Dining");
            int idAmtDisabled = adminCtx.transactionDAO.addTransaction(tAmtDisabled);
            tAmtDisabled.setTxnId(idAmtDisabled);
            DetectionResult rAmtDisabled = adminCtx.detectionService.evaluate(tAmtDisabled);
            assertTrue("Amount Rule Disabled Evaluation", !hasReason(rAmtDisabled, "Amount Rule"),
                    "20000 was NOT flagged by Amount Rule while disabled.");

            adminCtx.detectionService.setDetectorEnabled("Amount Rule", true);
            assertTrue("Re-enable Amount Rule", adminCtx.detectionService.isDetectorEnabled("Amount Rule"), "Amount Rule re-enabled");

            Transaction tAmtEnabled = new Transaction(testUser.getUserId(), 20000.0, testUser.getUsualLocation(), "Dining");
            int idAmtEnabled = adminCtx.transactionDAO.addTransaction(tAmtEnabled);
            tAmtEnabled.setTxnId(idAmtEnabled);
            DetectionResult rAmtEnabled = adminCtx.detectionService.evaluate(tAmtEnabled);
            assertTrue("Amount Rule Re-enabled Evaluation", hasReason(rAmtEnabled, "Amount Rule"),
                    "20000 was flagged by Amount Rule after re-enabling.");
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        // 5.2 Velocity Rule Toggle
        try {
            adminCtx.velocityRule.setMaxTransactions(1);
            adminCtx.detectionService.setDetectorEnabled("Velocity Rule", false);
            assertTrue("Disable Velocity Rule", !adminCtx.detectionService.isDetectorEnabled("Velocity Rule"), "Velocity Rule disabled");

            Transaction tVelDisabled = new Transaction(velocityUser.getUserId(), 100.0, "Delhi", "Dining");
            int idVelDisabled = adminCtx.transactionDAO.addTransaction(tVelDisabled);
            tVelDisabled.setTxnId(idVelDisabled);
            DetectionResult rVelDisabled = adminCtx.detectionService.evaluate(tVelDisabled);
            assertTrue("Velocity Rule Disabled Evaluation", !hasReason(rVelDisabled, "Velocity Rule"),
                    "Txn exceeding velocity was NOT flagged while disabled.");

            adminCtx.detectionService.setDetectorEnabled("Velocity Rule", true);
            Transaction tVelEnabled = new Transaction(velocityUser.getUserId(), 100.0, "Delhi", "Dining");
            int idVelEnabled = adminCtx.transactionDAO.addTransaction(tVelEnabled);
            tVelEnabled.setTxnId(idVelEnabled);
            DetectionResult rVelEnabled = adminCtx.detectionService.evaluate(tVelEnabled);
            assertTrue("Velocity Rule Re-enabled Evaluation", hasReason(rVelEnabled, "Velocity Rule"),
                    "Txn exceeding velocity was flagged after re-enabling.");
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        // 5.3 Location Rule Toggle
        try {
            adminCtx.detectionService.setDetectorEnabled("Location Rule", false);
            assertTrue("Disable Location Rule", !adminCtx.detectionService.isDetectorEnabled("Location Rule"), "Location Rule disabled");

            Transaction tLocDisabled = new Transaction(testUser.getUserId(), 100.0, "DifferentCityXYZ", "Dining");
            int idLocDisabled = adminCtx.transactionDAO.addTransaction(tLocDisabled);
            tLocDisabled.setTxnId(idLocDisabled);
            DetectionResult rLocDisabled = adminCtx.detectionService.evaluate(tLocDisabled);
            assertTrue("Location Rule Disabled Evaluation", !hasReason(rLocDisabled, "Location Rule"),
                    "Different location was NOT flagged while disabled.");

            adminCtx.detectionService.setDetectorEnabled("Location Rule", true);
            Transaction tLocEnabled = new Transaction(testUser.getUserId(), 100.0, "DifferentCityXYZ", "Dining");
            int idLocEnabled = adminCtx.transactionDAO.addTransaction(tLocEnabled);
            tLocEnabled.setTxnId(idLocEnabled);
            DetectionResult rLocEnabled = adminCtx.detectionService.evaluate(tLocEnabled);
            assertTrue("Location Rule Re-enabled Evaluation", hasReason(rLocEnabled, "Location Rule"),
                    "Different location was flagged after re-enabling.");
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        // 5.4 ML Classifier Toggle
        try {
            adminCtx.detectionService.setDetectorEnabled("ML Classifier (J48)", false);
            assertTrue("Disable ML Classifier", !adminCtx.detectionService.isDetectorEnabled("ML Classifier (J48)"), "ML Classifier disabled");

            Transaction tMLDisabled = new Transaction(testUser.getUserId(), 9000.0, "Unknown-VPN", "Electronics");
            int idMLDisabled = adminCtx.transactionDAO.addTransaction(tMLDisabled);
            tMLDisabled.setTxnId(idMLDisabled);
            DetectionResult rMLDisabled = adminCtx.detectionService.evaluate(tMLDisabled);
            assertTrue("ML Classifier Disabled Evaluation", !hasReason(rMLDisabled, "ML Classifier (J48)"),
                    "ML Classifier did NOT flag while disabled.");

            adminCtx.detectionService.setDetectorEnabled("ML Classifier (J48)", true);
            Transaction tMLEnabled = new Transaction(testUser.getUserId(), 9000.0, "Unknown-VPN", "Electronics");
            int idMLEnabled = adminCtx.transactionDAO.addTransaction(tMLEnabled);
            tMLEnabled.setTxnId(idMLEnabled);
            DetectionResult rMLEnabled = adminCtx.detectionService.evaluate(tMLEnabled);
            assertTrue("ML Classifier Re-enabled Evaluation", hasReason(rMLEnabled, "ML Classifier (J48)"),
                    "ML Classifier flagged after re-enabling.");
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        // =========================================================
        // SCENARIO 6: MULTI-PROCESS SIMULATION (FILE-BACKED SYNC)
        // =========================================================
        System.out.println("\n--- Testing Scenario 6: Separate Context / Process Sync ---");
        try {
            // Context A (Admin process) sets threshold to 8888.0
            adminCtx.amountRule.setThreshold(8888.0);
            adminCtx.detectionService.setDetectorEnabled("Amount Rule", true);

            // Context B (Customer process running with a separate newly constructed instance)
            FraudDetectionConfig separateConfig = new FraudDetectionConfig();
            assertTrue("Separate Config Reads Updated Threshold",
                    separateConfig.getAmountThreshold() == 8888.0,
                    "Separate process reads: " + separateConfig.getAmountThreshold());

            // Context A updates to 12345.0
            adminCtx.amountRule.setThreshold(12345.0);
            assertTrue("Separate Config Auto-Refreshes On Change",
                    separateConfig.getAmountThreshold() == 12345.0,
                    "Separate process auto-refreshed: " + separateConfig.getAmountThreshold());

            // Reset back to standard 5000.0, 3, 5 for clean state
            adminCtx.amountRule.setThreshold(5000.0);
            adminCtx.velocityRule.setMaxTransactions(3);
            adminCtx.velocityRule.setWindowMinutes(5);
            adminCtx.detectionService.setDetectorEnabled("Amount Rule", true);
            adminCtx.detectionService.setDetectorEnabled("Velocity Rule", true);
            adminCtx.detectionService.setDetectorEnabled("Location Rule", true);
            adminCtx.detectionService.setDetectorEnabled("ML Classifier (J48)", true);
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        System.out.println("\n=================================================");
        System.out.println("TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
