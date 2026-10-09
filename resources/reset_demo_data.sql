-- Resets the database to a clean demo state: removes all test transactions/alerts
-- and stray test users, then inserts a small, realistic set of sample data.
-- Stop both apps before running this, then restart them.
USE fraud_detection;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE alerts;
TRUNCATE TABLE transactions;
SET FOREIGN_KEY_CHECKS = 1;

-- Keep the 3 demo customers plus your own signed-up account; remove everything else
DELETE FROM users WHERE username NOT IN ('rahul_k', 'priya_s', 'aman_v', 'saksham');

-- Normal (approved) transactions
INSERT INTO transactions (user_id, amount, location, merchant_category, status) VALUES
  ((SELECT user_id FROM users WHERE username = 'rahul_k'), 250.00,  'Delhi',     'Groceries',   'APPROVED'),
  ((SELECT user_id FROM users WHERE username = 'priya_s'), 340.00,  'Mumbai',    'Dining',      'APPROVED'),
  ((SELECT user_id FROM users WHERE username = 'aman_v'),  1200.00, 'Bangalore', 'Electronics', 'APPROVED'),
  ((SELECT user_id FROM users WHERE username = 'priya_s'), 780.50,  'Mumbai',    'Travel',      'APPROVED');

-- A flagged transaction still waiting for the customer's response (shows as PENDING)
INSERT INTO transactions (user_id, amount, location, merchant_category, status)
VALUES ((SELECT user_id FROM users WHERE username = 'rahul_k'), 15000.00, 'Unknown-VPN', 'Electronics', 'FLAGGED');
INSERT INTO alerts (txn_id, risk_level, reason)
VALUES (LAST_INSERT_ID(), 'MEDIUM',
  '[Amount Rule] Amount 15000.0 exceeds threshold of 10000.0; [Location Rule] Transaction location ''Unknown-VPN'' differs from usual location ''Delhi''');

-- A flagged transaction the customer denied (shows as BLOCKED)
INSERT INTO transactions (user_id, amount, location, merchant_category, status)
VALUES ((SELECT user_id FROM users WHERE username = 'aman_v'), 640.00, 'Chennai', 'Dining', 'BLOCKED');
INSERT INTO alerts (txn_id, risk_level, reason)
VALUES (LAST_INSERT_ID(), 'LOW',
  '[Location Rule] Transaction location ''Chennai'' differs from usual location ''Bangalore''');
