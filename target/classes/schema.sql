-- Fraud Detection System — MySQL Schema
-- Run: mysql -u root -p < schema.sql
-- (PowerShell: Get-Content schema.sql | mysql -u root -p)

CREATE DATABASE IF NOT EXISTS fraud_detection;
USE fraud_detection;

CREATE TABLE IF NOT EXISTS users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    email          VARCHAR(100) NOT NULL UNIQUE,
    usual_location VARCHAR(100),
    -- Stored as plain text for this project only. A real system must store a
    -- salted hash (e.g. BCrypt) and never keep or select back a raw password.
    password_hash  VARCHAR(255) NOT NULL DEFAULT 'changeme'
);

CREATE TABLE IF NOT EXISTS transactions (
    txn_id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id           INT NOT NULL,
    amount            DECIMAL(12,2) NOT NULL,
    location          VARCHAR(100),
    merchant_category VARCHAR(100),
    timestamp         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS alerts (
    alert_id   INT AUTO_INCREMENT PRIMARY KEY,
    txn_id     INT NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    reason     VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (txn_id) REFERENCES transactions(txn_id)
);

-- Sample data so both apps have something to show immediately.
-- Customer-side login: try username "rahul_k" / password "pass123"
INSERT INTO users (username, email, usual_location, password_hash) VALUES
    ('rahul_k', 'rahul.k@example.com', 'Delhi', 'pass123'),
    ('priya_s', 'priya.s@example.com', 'Mumbai', 'pass123'),
    ('aman_v', 'aman.v@example.com', 'Bangalore', 'pass123');

INSERT INTO transactions (user_id, amount, location, merchant_category, status) VALUES
    (1, 250.00, 'Delhi', 'Groceries', 'APPROVED'),
    (2, 340.00, 'Mumbai', 'Dining', 'APPROVED'),
    (1, 8700.00, 'Unknown-VPN', 'Electronics', 'FLAGGED');

INSERT INTO alerts (txn_id, risk_level, reason) VALUES
    (3, 'HIGH', '[Amount Rule] Amount 8700.0 exceeds threshold of 5000.0; [Location Rule] Transaction location differs from usual location');

-- Useful queries for the DAO layer / viva demo
-- SELECT * FROM transactions WHERE status = 'FLAGGED';
-- SELECT t.txn_id, t.amount, a.risk_level, a.reason
--   FROM transactions t JOIN alerts a ON t.txn_id = a.txn_id;
-- SELECT COUNT(*) FROM transactions WHERE user_id = 1 AND timestamp >= (NOW() - INTERVAL 5 MINUTE);
