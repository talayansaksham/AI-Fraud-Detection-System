package com.frauddetection.service;

import com.frauddetection.model.Transaction;

public interface FraudDetector {
    String check(Transaction txn);
    String getName();
}
