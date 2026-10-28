-- ============================================================
-- ATM Transaction System — Useful Queries & Reports
-- ============================================================

USE atm_transaction_system;

-- ============================================================
-- Q1: View all customers with their account details
-- ============================================================
SELECT 
    c.customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS full_name,
    c.email,
    c.phone,
    a.account_number,
    a.account_type,
    a.balance,
    a.status AS account_status
FROM customers c
JOIN accounts a ON c.customer_id = a.customer_id
ORDER BY c.customer_id;

-- ============================================================
-- Q2: View all ATM cards with account and customer info
-- ============================================================
SELECT 
    ac.card_number,
    ac.card_status,
    ac.expiry_date,
    ac.failed_attempts,
    a.account_number,
    a.balance,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name
FROM atm_cards ac
JOIN accounts a ON ac.account_id = a.account_id
JOIN customers c ON a.customer_id = c.customer_id
ORDER BY ac.card_id;

-- ============================================================
-- Q3: Transaction history for a specific account (newest first)
-- ============================================================
SELECT 
    t.transaction_ref,
    t.transaction_type,
    t.amount,
    t.balance_after,
    t.status,
    t.description,
    t.transfer_ref,
    t.transaction_date
FROM transactions t
WHERE t.account_id = 1
ORDER BY t.transaction_date DESC
LIMIT 10;

-- ============================================================
-- Q4: Daily transaction summary report
-- ============================================================
SELECT 
    DATE(t.transaction_date) AS txn_date,
    t.transaction_type,
    COUNT(*) AS total_transactions,
    SUM(t.amount) AS total_amount,
    SUM(CASE WHEN t.status = 'SUCCESS' THEN 1 ELSE 0 END) AS successful,
    SUM(CASE WHEN t.status = 'FAILED' THEN 1 ELSE 0 END) AS failed
FROM transactions t
GROUP BY DATE(t.transaction_date), t.transaction_type
ORDER BY txn_date DESC, t.transaction_type;

-- ============================================================
-- Q5: Accounts with balance below minimum required
-- ============================================================
SELECT 
    a.account_number,
    a.balance,
    a.min_balance,
    (a.min_balance - a.balance) AS shortfall,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name
FROM accounts a
JOIN customers c ON a.customer_id = c.customer_id
WHERE a.balance < a.min_balance AND a.status = 'ACTIVE'
ORDER BY shortfall DESC;

-- ============================================================
-- Q6: Blocked ATM cards with customer details
-- ============================================================
SELECT 
    ac.card_number,
    ac.failed_attempts,
    ac.blocked_until,
    a.account_number,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.phone
FROM atm_cards ac
JOIN accounts a ON ac.account_id = a.account_id
JOIN customers c ON a.customer_id = c.customer_id
WHERE ac.card_status = 'BLOCKED';

-- ============================================================
-- Q7: Total deposits and withdrawals per customer
-- ============================================================
SELECT 
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    SUM(CASE WHEN t.transaction_type = 'DEPOSIT' THEN t.amount ELSE 0 END) AS total_deposits,
    SUM(CASE WHEN t.transaction_type = 'WITHDRAWAL' THEN t.amount ELSE 0 END) AS total_withdrawals,
    COUNT(*) AS total_transactions
FROM customers c
JOIN accounts a ON c.customer_id = a.customer_id
JOIN transactions t ON a.account_id = t.account_id
WHERE t.status = 'SUCCESS'
GROUP BY c.customer_id
ORDER BY total_transactions DESC;

-- ============================================================
-- Q8: Transfer history with source and destination details
-- ============================================================
SELECT 
    tr.transfer_ref,
    sa.account_number AS source_account,
    da.account_number AS dest_account,
    tr.amount,
    tr.status,
    tr.description,
    tr.transfer_date
FROM transfers tr
JOIN accounts sa ON tr.source_account_id = sa.account_id
JOIN accounts da ON tr.dest_account_id = da.account_id
ORDER BY tr.transfer_date DESC;

-- ============================================================
-- Q9: Failed login attempts in the last 24 hours
-- ============================================================
SELECT 
    la.card_number,
    la.attempt_type,
    la.failure_reason,
    la.attempt_time
FROM login_attempts la
WHERE la.success = FALSE 
  AND la.attempt_time >= NOW() - INTERVAL 1 DAY
ORDER BY la.attempt_time DESC;

-- ============================================================
-- Q10: Account balance summary statistics
-- ============================================================
SELECT 
    a.account_type,
    COUNT(*) AS total_accounts,
    MIN(a.balance) AS min_balance,
    MAX(a.balance) AS max_balance,
    AVG(a.balance) AS avg_balance,
    SUM(a.balance) AS total_balance
FROM accounts a
WHERE a.status = 'ACTIVE'
GROUP BY a.account_type;
