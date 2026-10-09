-- Run this ONLY if you already created the fraud_detection database before
-- the customer-login feature was added (i.e. you get an "Unknown column
-- 'password_hash'" error). If you're setting up fresh, just use schema.sql
-- instead — you don't need this file at all.

USE fraud_detection;

ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT 'changeme';

-- Give the existing sample users a real demo password instead of the default
UPDATE users SET password_hash = 'pass123' WHERE username IN ('rahul_k', 'priya_s', 'aman_v');
