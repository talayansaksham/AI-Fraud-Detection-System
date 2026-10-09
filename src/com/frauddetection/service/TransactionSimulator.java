package com.frauddetection.service;

import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class TransactionSimulator implements Runnable {
    public interface Listener {
        void onTransactionGenerated(Transaction txn, DetectionResult result);
    }

    private final TransactionDAO transactionDAO;
    private final FraudDetectionService detectionService;
    private final List<User> users;
    private final Listener listener;
    private final Random random = new Random();
    private boolean running = false;

    private final String[] sampleLocations = {"Delhi", "Mumbai", "Bangalore", "Chennai", "Unknown-VPN"};
    private final String[] sampleCategories = {"Groceries", "Electronics", "Travel", "Dining", "ATM Withdrawal"};

    public TransactionSimulator(TransactionDAO transactionDAO, FraudDetectionService detectionService,
                                 List<User> users, Listener listener) {
        this.transactionDAO = transactionDAO;
        this.detectionService = detectionService;
        this.users = users;
        this.listener = listener;
    }

    public synchronized void startSimulation() { running = true; }
    public synchronized void stopSimulation() { running = false; }
    public synchronized boolean isRunning() { return running; }

    @Override
    public void run() {
        startSimulation();
        while (isRunning()) {
            try {
                if (!users.isEmpty()) {
                    Transaction txn = generateRandomTransaction();
                    int txnId = transactionDAO.addTransaction(txn);
                    txn.setTxnId(txnId);
                    DetectionResult result = detectionService.evaluate(txn);
                    if (listener != null) listener.onTransactionGenerated(txn, result);
                }
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (SQLException e) {
                System.err.println("TransactionSimulator DB error: " + e.getMessage());
            }
        }
    }

    private Transaction generateRandomTransaction() {
        User user = users.get(random.nextInt(users.size()));
        double amount = 100 + random.nextDouble() * 9900;
        String location = sampleLocations[random.nextInt(sampleLocations.length)];
        String category = sampleCategories[random.nextInt(sampleCategories.length)];
        return new Transaction(user.getUserId(), amount, location, category);
    }
}
