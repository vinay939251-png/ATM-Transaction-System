-- ============================================================
-- ATM Transaction System — Sample Data
-- All data is synthetic / fictional for demonstration only.
-- PINs are BCrypt hashes (cost=12, verified):
--   PIN "1234"     -> $2a$12$NswJp0p8g7nSagMizknac.ItdeNyKEVAeyTWlVsWxjsGLR9uXrd3y
--   PIN "5678"     -> $2a$12$uxArKDs.1U5H2hG6wGQaDeUXiIIBjtYGgJYVCUSSP7RZ7hLVyz1su
--   PIN "9012"     -> $2a$12$X8BSdeJ7i9qxfMQx5WXZCeCKdutnbW7Y9bAfgqQqiVWMnd2J2dpyK
--   PIN "3456"     -> $2a$12$mNU330fsj73YRi6/NLhkCu0Mdtze8bXLLb/MODfrQojWly7bBKoRi
--   PIN "7890"     -> $2a$12$4VI4wh95RI0lUgbBK0e1.ueVyk1Kd15ApL7QdthNhHyOZXylHTEO2
--
-- Admin password "admin123" -> $2a$12$5crfbcYFJIvCcSAB5ec5uuhHKWlv0b28aub8T3PkQen/fpk7pC24q
-- ============================================================

USE atm_transaction_system;

-- ============================================================
-- CUSTOMERS — 5 sample customers
-- ============================================================
INSERT INTO customers (first_name, last_name, date_of_birth, gender, email, phone, address, city, state, zip_code) VALUES
('Aarav',    'Sharma',   '1995-03-15', 'M', 'aarav.sharma@example.com',    '9876543210', '42 MG Road, Banjara Hills',       'Hyderabad',  'Telangana',    '500034'),
('Priya',    'Reddy',    '1998-07-22', 'F', 'priya.reddy@example.com',     '9876543211', '18 Jubilee Hills, Road No 36',    'Hyderabad',  'Telangana',    '500033'),
('Vikram',   'Patel',    '1990-11-08', 'M', 'vikram.patel@example.com',    '9876543212', '7 SG Highway, Satellite',         'Ahmedabad',  'Gujarat',      '380015'),
('Sneha',    'Iyer',     '1993-01-30', 'F', 'sneha.iyer@example.com',      '9876543213', '23 Anna Salai, T Nagar',          'Chennai',    'Tamil Nadu',   '600017'),
('Rohan',    'Kumar',    '2000-05-12', 'M', 'rohan.kumar@example.com',     '9876543214', '56 Connaught Place, Block A',     'New Delhi',  'Delhi',        '110001');

-- ============================================================
-- ACCOUNTS — Each customer gets at least one account
-- ============================================================
INSERT INTO accounts (account_number, customer_id, account_type, balance, min_balance, status) VALUES
('ACC1001000001', 1, 'SAVINGS', 75000.00, 1000.00, 'ACTIVE'),
('ACC1001000002', 1, 'CURRENT', 150000.00, 5000.00, 'ACTIVE'),
('ACC1002000001', 2, 'SAVINGS', 32500.50, 1000.00, 'ACTIVE'),
('ACC1003000001', 3, 'SAVINGS', 120000.00, 1000.00, 'ACTIVE'),
('ACC1003000002', 3, 'CURRENT', 500000.00, 5000.00, 'ACTIVE'),
('ACC1004000001', 4, 'SAVINGS', 45000.75, 1000.00, 'ACTIVE'),
('ACC1005000001', 5, 'SAVINGS', 8500.00, 1000.00, 'ACTIVE');

-- ============================================================
-- ATM_CARDS — One card per account
-- PIN hashes generated with BCrypt (cost factor 12)
-- ============================================================
INSERT INTO atm_cards (card_number, account_id, pin_hash, card_status, expiry_date, issued_date, failed_attempts) VALUES
('4532-1234-5678-9010', 1, '$2a$12$NswJp0p8g7nSagMizknac.ItdeNyKEVAeyTWlVsWxjsGLR9uXrd3y', 'ACTIVE',  '2028-03-31', '2024-04-01', 0),
('4532-2345-6789-0121', 2, '$2a$12$NswJp0p8g7nSagMizknac.ItdeNyKEVAeyTWlVsWxjsGLR9uXrd3y', 'ACTIVE',  '2028-03-31', '2024-04-01', 0),
('4532-3456-7890-1232', 3, '$2a$12$NswJp0p8g7nSagMizknac.ItdeNyKEVAeyTWlVsWxjsGLR9uXrd3y', 'ACTIVE',  '2028-12-31', '2023-01-01', 0),
('4532-4567-8901-2343', 4, '$2a$12$uxArKDs.1U5H2hG6wGQaDeUXiIIBjtYGgJYVCUSSP7RZ7hLVyz1su', 'ACTIVE',  '2028-06-30', '2024-07-01', 0),
('4532-5678-9012-3454', 5, '$2a$12$X8BSdeJ7i9qxfMQx5WXZCeCKdutnbW7Y9bAfgqQqiVWMnd2J2dpyK', 'ACTIVE',  '2028-09-30', '2024-10-01', 0),
('4532-6789-0123-4565', 6, '$2a$12$mNU330fsj73YRi6/NLhkCu0Mdtze8bXLLb/MODfrQojWly7bBKoRi', 'ACTIVE',  '2027-06-30', '2023-07-01', 0),
('4532-7890-1234-5676', 7, '$2a$12$4VI4wh95RI0lUgbBK0e1.ueVyk1Kd15ApL7QdthNhHyOZXylHTEO2', 'BLOCKED', '2028-12-31', '2025-01-01', 3);

-- ============================================================
-- ADMINS — One admin user for demonstration
-- Password: "admin123" (BCrypt hashed)
-- ============================================================
INSERT INTO admins (username, password_hash, full_name, role, is_active) VALUES
('admin', '$2a$12$5crfbcYFJIvCcSAB5ec5uuhHKWlv0b28aub8T3PkQen/fpk7pC24q', 'System Administrator', 'SUPER_ADMIN', TRUE);

-- ============================================================
-- TRANSACTIONS — Some sample transaction history
-- ============================================================
INSERT INTO transactions (transaction_ref, account_id, card_id, transaction_type, amount, balance_after, status, description) VALUES
(UUID(), 1, 1, 'DEPOSIT',      50000.00, 125000.00, 'SUCCESS', 'Initial deposit'),
(UUID(), 1, 1, 'WITHDRAWAL',   5000.00,  120000.00, 'SUCCESS', 'ATM withdrawal'),
(UUID(), 1, 1, 'WITHDRAWAL',   20000.00, 100000.00, 'SUCCESS', 'ATM withdrawal'),
(UUID(), 1, 1, 'DEPOSIT',      10000.00, 110000.00, 'SUCCESS', 'Cash deposit'),
(UUID(), 3, 3, 'DEPOSIT',      15000.00, 47500.50,  'SUCCESS', 'Cash deposit'),
(UUID(), 3, 3, 'WITHDRAWAL',   2000.00,  45500.50,  'SUCCESS', 'ATM withdrawal'),
(UUID(), 4, 4, 'DEPOSIT',      100000.00,220000.00, 'SUCCESS', 'Salary credit'),
(UUID(), 4, 4, 'WITHDRAWAL',   10000.00, 210000.00, 'SUCCESS', 'ATM withdrawal');

-- ============================================================
-- SAMPLE TRANSFER
-- ============================================================
SET @transfer_uuid = UUID();

INSERT INTO transfers (transfer_ref, source_account_id, dest_account_id, amount, status, description) VALUES
(@transfer_uuid, 1, 3, 5000.00, 'SUCCESS', 'Transfer to Priya');

INSERT INTO transactions (transaction_ref, account_id, card_id, transaction_type, amount, balance_after, status, description, transfer_ref) VALUES
(UUID(), 1, 1, 'TRANSFER_DEBIT',  5000.00, 70000.00, 'SUCCESS', 'Transfer to ACC1002000001', @transfer_uuid),
(UUID(), 3, NULL, 'TRANSFER_CREDIT', 5000.00, 37500.50, 'SUCCESS', 'Transfer from ACC1001000001', @transfer_uuid);
