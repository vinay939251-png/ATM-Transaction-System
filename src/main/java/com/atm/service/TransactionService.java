package com.atm.service;

import com.atm.dao.ATMCardDAO;
import com.atm.dao.AccountDAO;
import com.atm.dao.TransactionDAO;
import com.atm.dao.TransferDAO;
import com.atm.model.*;
import com.atm.util.ConfigManager;
import com.atm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Core service layer for ATM transaction operations.
 * 
 * Implements ACID transaction processing:
 * - Atomicity: All steps in a transaction commit or roll back together
 * - Consistency: Conditional UPDATEs prevent negative balances
 * - Isolation: Uses JDBC transaction boundaries
 * - Durability: MySQL InnoDB ensures committed data survives crashes
 * 
 * All financial operations use DECIMAL types, never floating-point.
 */
public class TransactionService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TransferDAO transferDAO = new TransferDAO();
    private final ATMCardDAO cardDAO = new ATMCardDAO();
    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();
    private final ConfigManager config = ConfigManager.getInstance();

    /**
     * Result of a transaction operation.
     */
    public record TransactionResult(boolean success, String message, String transactionRef, BigDecimal newBalance) {
        public static TransactionResult success(String msg, String ref, BigDecimal balance) {
            return new TransactionResult(true, msg, ref, balance);
        }
        public static TransactionResult failure(String msg) {
            return new TransactionResult(false, msg, null, null);
        }
    }

    /**
     * Retrieves the current balance from the database (not from cached memory).
     */
    public BigDecimal getBalance(int accountId) throws SQLException {
        return accountDAO.getBalance(accountId);
    }

    /**
     * Processes a cash withdrawal.
     * 
     * Transaction flow:
     * 1. Validate amount (positive, multiple of denomination, within limits)
     * 2. Check daily withdrawal limit
     * 3. BEGIN TRANSACTION
     * 4. Atomically debit the account (conditional UPDATE prevents overdraft)
     * 5. Read the new balance
     * 6. Insert transaction record
     * 7. Update daily withdrawal tracking
     * 8. COMMIT (or ROLLBACK on failure)
     */
    public TransactionResult withdraw(ATMCard card, BigDecimal amount) {
        // === Pre-validation (before touching the database) ===
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return TransactionResult.failure("Withdrawal amount must be positive.");
        }

        // Check denomination (must be multiple of 100)
        if (amount.remainder(new BigDecimal("100")).compareTo(BigDecimal.ZERO) != 0) {
            return TransactionResult.failure("Amount must be a multiple of ₹100.");
        }

        // Check per-transaction limit
        BigDecimal maxPerTxn = config.getMaxWithdrawalPerTransaction();
        if (amount.compareTo(maxPerTxn) > 0) {
            return TransactionResult.failure("Maximum withdrawal per transaction is ₹" + maxPerTxn.toPlainString() + ".");
        }

        Connection conn = null;
        try {
            // Check daily withdrawal limit
            BigDecimal dailyTotal = cardDAO.getDailyWithdrawalTotal(card.getCardId());
            BigDecimal maxDaily = config.getMaxDailyWithdrawal();
            if (dailyTotal.add(amount).compareTo(maxDaily) > 0) {
                BigDecimal remaining = maxDaily.subtract(dailyTotal);
                return TransactionResult.failure(
                    "Daily withdrawal limit exceeded. Remaining today: ₹" + remaining.toPlainString()
                );
            }

            // === Database Transaction ===
            conn = dbPool.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // Atomically debit — conditional UPDATE prevents overdraft
            int rowsUpdated = accountDAO.debit(card.getAccountId(), amount, conn);

            if (rowsUpdated == 0) {
                conn.rollback();
                return TransactionResult.failure("Insufficient balance for this withdrawal.");
            }

            // Read the new balance (within the same transaction for consistency)
            BigDecimal newBalance = getBalanceInTransaction(card.getAccountId(), conn);

            // Insert the transaction record
            Transaction txn = new Transaction();
            txn.setAccountId(card.getAccountId());
            txn.setCardId(card.getCardId());
            txn.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
            txn.setAmount(amount);
            txn.setBalanceAfter(newBalance);
            txn.setStatus(Transaction.TransactionStatus.SUCCESS);
            txn.setDescription("ATM cash withdrawal");

            String ref = transactionDAO.insert(txn, conn);

            // Update daily withdrawal tracking
            cardDAO.updateDailyWithdrawal(card.getCardId(), amount, conn);

            // COMMIT — all operations succeed together
            conn.commit();

            return TransactionResult.success(
                "Withdrawal of ₹" + amount.toPlainString() + " successful.",
                ref, newBalance
            );

        } catch (SQLException e) {
            // ROLLBACK on any failure
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[Transaction] Withdrawal failed: " + e.getMessage());
            return TransactionResult.failure("Transaction failed due to a system error. No money was debited.");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                dbPool.releaseConnection(conn);
            }
        }
    }

    /**
     * Processes a cash deposit.
     * 
     * Transaction flow:
     * 1. Validate amount
     * 2. BEGIN TRANSACTION
     * 3. Credit the account
     * 4. Read the new balance
     * 5. Insert transaction record
     * 6. COMMIT (or ROLLBACK on failure)
     */
    public TransactionResult deposit(ATMCard card, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return TransactionResult.failure("Deposit amount must be positive.");
        }

        if (amount.remainder(new BigDecimal("100")).compareTo(BigDecimal.ZERO) != 0) {
            return TransactionResult.failure("Amount must be a multiple of ₹100.");
        }

        Connection conn = null;
        try {
            conn = dbPool.getConnection();
            conn.setAutoCommit(false);

            int rowsUpdated = accountDAO.credit(card.getAccountId(), amount, conn);
            if (rowsUpdated == 0) {
                conn.rollback();
                return TransactionResult.failure("Deposit failed. Account may be inactive.");
            }

            BigDecimal newBalance = getBalanceInTransaction(card.getAccountId(), conn);

            Transaction txn = new Transaction();
            txn.setAccountId(card.getAccountId());
            txn.setCardId(card.getCardId());
            txn.setTransactionType(Transaction.TransactionType.DEPOSIT);
            txn.setAmount(amount);
            txn.setBalanceAfter(newBalance);
            txn.setStatus(Transaction.TransactionStatus.SUCCESS);
            txn.setDescription("ATM cash deposit (simulated)");

            String ref = transactionDAO.insert(txn, conn);
            conn.commit();

            return TransactionResult.success(
                "Deposit of ₹" + amount.toPlainString() + " successful.",
                ref, newBalance
            );

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[Transaction] Deposit failed: " + e.getMessage());
            return TransactionResult.failure("Transaction failed due to a system error. Please try again.");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                dbPool.releaseConnection(conn);
            }
        }
    }

    /**
     * Processes a fund transfer between two accounts.
     * 
     * Atomic two-sided transfer:
     * 1. Validate source and destination
     * 2. Reject transfer to the same account
     * 3. BEGIN TRANSACTION
     * 4. Debit source (conditional — prevents overdraft)
     * 5. Credit destination
     * 6. Insert transfer record with shared reference
     * 7. Insert debit transaction record
     * 8. Insert credit transaction record
     * 9. COMMIT (or ROLLBACK if any step fails)
     */
    public TransactionResult transfer(ATMCard sourceCard, String destAccountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return TransactionResult.failure("Transfer amount must be positive.");
        }

        Connection conn = null;
        try {
            // Validate destination account exists
            Account destAccount = accountDAO.findByAccountNumber(destAccountNumber);
            if (destAccount == null) {
                return TransactionResult.failure("Destination account not found.");
            }

            // Reject transfer to same account
            if (destAccount.getAccountId() == sourceCard.getAccountId()) {
                return TransactionResult.failure("Cannot transfer to the same account.");
            }

            // Check destination account is active
            if (destAccount.getStatus() != Account.AccountStatus.ACTIVE) {
                return TransactionResult.failure("Destination account is not active.");
            }

            conn = dbPool.getConnection();
            conn.setAutoCommit(false);

            // Debit source — atomic conditional update
            int debitRows = accountDAO.debit(sourceCard.getAccountId(), amount, conn);
            if (debitRows == 0) {
                conn.rollback();
                return TransactionResult.failure("Insufficient balance for this transfer.");
            }

            // Credit destination
            int creditRows = accountDAO.credit(destAccount.getAccountId(), amount, conn);
            if (creditRows == 0) {
                conn.rollback();
                return TransactionResult.failure("Transfer failed. Destination account may be inactive.");
            }

            // Read new balances
            BigDecimal sourceNewBalance = getBalanceInTransaction(sourceCard.getAccountId(), conn);
            BigDecimal destNewBalance = getBalanceInTransaction(destAccount.getAccountId(), conn);

            // Insert transfer record with shared UUID reference
            Transfer transfer = new Transfer();
            transfer.setSourceAccountId(sourceCard.getAccountId());
            transfer.setDestAccountId(destAccount.getAccountId());
            transfer.setAmount(amount);
            transfer.setStatus(Transfer.TransferStatus.SUCCESS);
            transfer.setDescription("Transfer to " + destAccountNumber);

            String transferRef = transferDAO.insert(transfer, conn);

            // Insert debit transaction
            Transaction debitTxn = new Transaction();
            debitTxn.setAccountId(sourceCard.getAccountId());
            debitTxn.setCardId(sourceCard.getCardId());
            debitTxn.setTransactionType(Transaction.TransactionType.TRANSFER_DEBIT);
            debitTxn.setAmount(amount);
            debitTxn.setBalanceAfter(sourceNewBalance);
            debitTxn.setStatus(Transaction.TransactionStatus.SUCCESS);
            debitTxn.setDescription("Transfer to " + destAccountNumber);
            debitTxn.setTransferRef(transferRef);
            transactionDAO.insert(debitTxn, conn);

            // Insert credit transaction
            Transaction creditTxn = new Transaction();
            creditTxn.setAccountId(destAccount.getAccountId());
            creditTxn.setCardId(null);
            creditTxn.setTransactionType(Transaction.TransactionType.TRANSFER_CREDIT);
            creditTxn.setAmount(amount);
            creditTxn.setBalanceAfter(destNewBalance);
            creditTxn.setStatus(Transaction.TransactionStatus.SUCCESS);
            creditTxn.setDescription("Transfer from " + sourceCard.getAccount().getAccountNumber());
            creditTxn.setTransferRef(transferRef);
            transactionDAO.insert(creditTxn, conn);

            conn.commit();

            return TransactionResult.success(
                "Transfer of ₹" + amount.toPlainString() + " to " + destAccountNumber + " successful.",
                transferRef, sourceNewBalance
            );

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[Transaction] Transfer failed: " + e.getMessage());
            return TransactionResult.failure("Transfer failed due to a system error. No money was transferred.");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                dbPool.releaseConnection(conn);
            }
        }
    }

    /**
     * Gets the mini-statement (recent transactions) for an account.
     * Only returns transactions belonging to the specified account.
     */
    public List<Transaction> getMiniStatement(int accountId, int limit) throws SQLException {
        return transactionDAO.getRecentTransactions(accountId, limit);
    }

    /**
     * Reads balance within an active transaction (using same connection).
     */
    private BigDecimal getBalanceInTransaction(int accountId, Connection conn) throws SQLException {
        var stmt = conn.prepareStatement("SELECT balance FROM accounts WHERE account_id = ?");
        stmt.setInt(1, accountId);
        var rs = stmt.executeQuery();
        if (rs.next()) return rs.getBigDecimal("balance");
        throw new SQLException("Account not found during transaction: " + accountId);
    }
}
