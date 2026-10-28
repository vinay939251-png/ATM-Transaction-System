package com.atm.dao;

import com.atm.model.Transfer;
import com.atm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for Transfer records.
 */
public class TransferDAO {

    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();

    /**
     * Inserts a transfer record. Uses the caller's connection for transaction control.
     * Returns the generated transfer reference (UUID).
     */
    public String insert(Transfer transfer, Connection conn) throws SQLException {
        String ref = UUID.randomUUID().toString();
        String sql = "INSERT INTO transfers (transfer_ref, source_account_id, dest_account_id, " +
                     "amount, status, description) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ref);
            stmt.setInt(2, transfer.getSourceAccountId());
            stmt.setInt(3, transfer.getDestAccountId());
            stmt.setBigDecimal(4, transfer.getAmount());
            stmt.setString(5, transfer.getStatus().name());
            stmt.setString(6, transfer.getDescription());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    transfer.setTransferId(keys.getInt(1));
                }
            }
        }
        transfer.setTransferRef(ref);
        return ref;
    }

    /**
     * Gets all transfers (for admin view).
     */
    public List<Transfer> findAll(int limit) throws SQLException {
        String sql = "SELECT tr.*, sa.account_number AS source_acc_num, da.account_number AS dest_acc_num " +
                     "FROM transfers tr " +
                     "JOIN accounts sa ON tr.source_account_id = sa.account_id " +
                     "JOIN accounts da ON tr.dest_account_id = da.account_id " +
                     "ORDER BY tr.transfer_date DESC LIMIT ?";
        List<Transfer> transfers = new ArrayList<>();

        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transfers.add(mapTransfer(rs));
                }
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return transfers;
    }

    private Transfer mapTransfer(ResultSet rs) throws SQLException {
        Transfer t = new Transfer();
        t.setTransferId(rs.getInt("transfer_id"));
        t.setTransferRef(rs.getString("transfer_ref"));
        t.setSourceAccountId(rs.getInt("source_account_id"));
        t.setDestAccountId(rs.getInt("dest_account_id"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setStatus(Transfer.TransferStatus.valueOf(rs.getString("status")));
        t.setDescription(rs.getString("description"));
        t.setTransferDate(rs.getTimestamp("transfer_date").toLocalDateTime());

        // Mapped from JOIN
        try {
            t.setSourceAccountNumber(rs.getString("source_acc_num"));
            t.setDestAccountNumber(rs.getString("dest_acc_num"));
        } catch (SQLException ignored) {
            // Column not present if query doesn't JOIN
        }

        return t;
    }
}
