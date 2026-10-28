-- ============================================================
-- ATM Transaction System — Database Schema
-- Database: atm_transaction_system
-- Engine: MySQL 8.x / InnoDB
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS atm_transaction_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE atm_transaction_system;

-- ============================================================
-- 1. CUSTOMERS TABLE
-- Purpose: Stores personal information of bank customers.
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    customer_id     INT             AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(50)     NOT NULL,
    last_name       VARCHAR(50)     NOT NULL,
    date_of_birth   DATE            NOT NULL,
    gender          ENUM('M','F','O') NOT NULL DEFAULT 'O',
    email           VARCHAR(100)    NOT NULL UNIQUE,
    phone           VARCHAR(15)     NOT NULL UNIQUE,
    address         VARCHAR(255)    NOT NULL,
    city            VARCHAR(50)     NOT NULL,
    state           VARCHAR(50)     NOT NULL,
    zip_code        VARCHAR(10)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_customer_name (last_name, first_name),
    INDEX idx_customer_email (email)
) ENGINE=InnoDB;

-- ============================================================
-- 2. ACCOUNTS TABLE
-- Purpose: Bank accounts linked to customers.
-- Each customer can have multiple accounts (savings, current).
-- Balance uses DECIMAL for monetary precision.
-- ============================================================
CREATE TABLE IF NOT EXISTS accounts (
    account_id      INT             AUTO_INCREMENT PRIMARY KEY,
    account_number  VARCHAR(20)     NOT NULL UNIQUE,
    customer_id     INT             NOT NULL,
    account_type    ENUM('SAVINGS','CURRENT') NOT NULL DEFAULT 'SAVINGS',
    balance         DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    min_balance     DECIMAL(15,2)   NOT NULL DEFAULT 1000.00,
    status          ENUM('ACTIVE','INACTIVE','CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT fk_account_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    INDEX idx_account_number (account_number),
    INDEX idx_account_customer (customer_id)
) ENGINE=InnoDB;

-- ============================================================
-- 3. ATM_CARDS TABLE
-- Purpose: ATM cards associated with bank accounts.
-- PIN is stored as a BCrypt hash — never plaintext.
-- ============================================================
CREATE TABLE IF NOT EXISTS atm_cards (
    card_id         INT             AUTO_INCREMENT PRIMARY KEY,
    card_number     VARCHAR(19)     NOT NULL UNIQUE,  -- Format: XXXX-XXXX-XXXX-XXXX
    account_id      INT             NOT NULL,
    pin_hash        VARCHAR(255)    NOT NULL,          -- BCrypt hash of the PIN
    card_status     ENUM('ACTIVE','BLOCKED','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    expiry_date     DATE            NOT NULL,
    issued_date     DATE            NOT NULL DEFAULT (CURRENT_DATE),
    failed_attempts INT             NOT NULL DEFAULT 0,
    blocked_until   TIMESTAMP       NULL DEFAULT NULL,
    daily_withdrawal_total DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    last_withdrawal_date   DATE     NULL DEFAULT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_failed_attempts CHECK (failed_attempts >= 0),
    CONSTRAINT fk_card_account
        FOREIGN KEY (account_id) REFERENCES accounts(account_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    INDEX idx_card_number (card_number),
    INDEX idx_card_account (account_id)
) ENGINE=InnoDB;

-- ============================================================
-- 4. TRANSACTIONS TABLE
-- Purpose: Records all ATM transactions (withdrawals, deposits).
-- Tracks balance_after for audit trail / mini-statement.
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id      INT             AUTO_INCREMENT PRIMARY KEY,
    transaction_ref     VARCHAR(36)     NOT NULL UNIQUE,  -- UUID reference
    account_id          INT             NOT NULL,
    card_id             INT             NULL,              -- NULL for admin/system transactions
    transaction_type    ENUM('WITHDRAWAL','DEPOSIT','TRANSFER_DEBIT','TRANSFER_CREDIT') NOT NULL,
    amount              DECIMAL(15,2)   NOT NULL,
    balance_after       DECIMAL(15,2)   NOT NULL,
    status              ENUM('SUCCESS','FAILED','PENDING') NOT NULL DEFAULT 'SUCCESS',
    description         VARCHAR(255)    NULL,
    transfer_ref        VARCHAR(36)     NULL,              -- Shared reference for transfer pairs
    transaction_date    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT fk_txn_account
        FOREIGN KEY (account_id) REFERENCES accounts(account_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_txn_card
        FOREIGN KEY (card_id) REFERENCES atm_cards(card_id)
        ON UPDATE CASCADE ON DELETE SET NULL,

    INDEX idx_txn_account (account_id),
    INDEX idx_txn_date (transaction_date),
    INDEX idx_txn_ref (transaction_ref),
    INDEX idx_txn_transfer_ref (transfer_ref)
) ENGINE=InnoDB;

-- ============================================================
-- 5. TRANSFERS TABLE
-- Purpose: Records fund transfers between two accounts.
-- Links to debit and credit transaction records.
-- ============================================================
CREATE TABLE IF NOT EXISTS transfers (
    transfer_id         INT             AUTO_INCREMENT PRIMARY KEY,
    transfer_ref        VARCHAR(36)     NOT NULL UNIQUE,  -- Shared with transactions
    source_account_id   INT             NOT NULL,
    dest_account_id     INT             NOT NULL,
    amount              DECIMAL(15,2)   NOT NULL,
    status              ENUM('SUCCESS','FAILED','PENDING') NOT NULL DEFAULT 'SUCCESS',
    description         VARCHAR(255)    NULL,
    transfer_date       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_transfer_amount CHECK (amount > 0),
    CONSTRAINT chk_different_accounts CHECK (source_account_id <> dest_account_id),
    CONSTRAINT fk_transfer_source
        FOREIGN KEY (source_account_id) REFERENCES accounts(account_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_transfer_dest
        FOREIGN KEY (dest_account_id) REFERENCES accounts(account_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,

    INDEX idx_transfer_source (source_account_id),
    INDEX idx_transfer_dest (dest_account_id),
    INDEX idx_transfer_ref (transfer_ref)
) ENGINE=InnoDB;

-- ============================================================
-- 6. ADMINS TABLE
-- Purpose: Separate authentication for administrative users.
-- ============================================================
CREATE TABLE IF NOT EXISTS admins (
    admin_id        INT             AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,           -- BCrypt hash
    full_name       VARCHAR(100)    NOT NULL,
    role            ENUM('SUPER_ADMIN','ADMIN') NOT NULL DEFAULT 'ADMIN',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_admin_username (username)
) ENGINE=InnoDB;

-- ============================================================
-- 7. LOGIN_ATTEMPTS TABLE
-- Purpose: Audit log for all authentication attempts.
-- Used for security monitoring and lockout enforcement.
-- ============================================================
CREATE TABLE IF NOT EXISTS login_attempts (
    attempt_id      INT             AUTO_INCREMENT PRIMARY KEY,
    card_number     VARCHAR(19)     NULL,               -- ATM user login
    admin_username  VARCHAR(50)     NULL,               -- Admin login
    attempt_type    ENUM('ATM','ADMIN') NOT NULL,
    success         BOOLEAN         NOT NULL DEFAULT FALSE,
    ip_address      VARCHAR(45)     NULL,               -- Optional, for logging
    attempt_time    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    failure_reason  VARCHAR(100)    NULL,

    INDEX idx_attempt_card (card_number),
    INDEX idx_attempt_time (attempt_time)
) ENGINE=InnoDB;
