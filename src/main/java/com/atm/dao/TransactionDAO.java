package com.atm.dao;

import com.atm.model.Transaction;
import com.atm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for Transaction records.
 * Uses prepared statements throughout.
 */
public class TransactionDAO {

    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();

    /**
     * Inserts a transaction record. Uses the caller's connection for transaction control.
     * Generates a UUID transaction reference.
     */
    public String insert(Transaction txn, Connection conn) throws SQLException {
        String ref = UUID.randomUUID().toString();
        String sql = "INSERT INTO transactions (transaction_ref, account_id, card_id, transaction_type, " +
                     "amount, balance_after, status, description, transfer_ref) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ref);
            stmt.setInt(2, txn.getAccountId());
            if (txn.getCardId() != null) {
                stmt.setInt(3, txn.getCardId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, txn.getTransactionType().name());
            stmt.setBigDecimal(5, txn.getAmount());
            stmt.setBigDecimal(6, txn.getBalanceAfter());
            stmt.setString(7, txn.getStatus().name());
            stmt.setString(8, txn.getDescription());
            stmt.setString(9, txn.getTransferRef());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    txn.setTransactionId(keys.getInt(1));
                }
            }
        }
        txn.setTransactionRef(ref);
        return ref;
    }

    /**
     * Gets recent transactions for an account (mini-statement).
     * Ordered newest first, limited to the specified count.
     * Only returns transactions for the specified account — prevents cross-account access.
     */
    public List<Transaction> getRecentTransactions(int accountId, int limit) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC LIMIT ?";
        List<Transaction> transactions = new ArrayList<>();

        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapTransaction(rs));
                }
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return transactions;
    }

    /**
     * Gets all transactions (for admin).
     */
    public List<Transaction> findAll(int limit) throws SQLException {
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC LIMIT ?";
        List<Transaction> transactions = new ArrayList<>();

        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapTransaction(rs));
                }
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return transactions;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction txn = new Transaction();
        txn.setTransactionId(rs.getInt("transaction_id"));
        txn.setTransactionRef(rs.getString("transaction_ref"));
        txn.setAccountId(rs.getInt("account_id"));

        int cardId = rs.getInt("card_id");
        txn.setCardId(rs.wasNull() ? null : cardId);

        txn.setTransactionType(Transaction.TransactionType.valueOf(rs.getString("transaction_type")));
        txn.setAmount(rs.getBigDecimal("amount"));
        txn.setBalanceAfter(rs.getBigDecimal("balance_after"));
        txn.setStatus(Transaction.TransactionStatus.valueOf(rs.getString("status")));
        txn.setDescription(rs.getString("description"));
        txn.setTransferRef(rs.getString("transfer_ref"));
        txn.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());

        return txn;
    }
}
