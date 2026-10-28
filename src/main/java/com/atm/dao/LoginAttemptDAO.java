package com.atm.dao;

import com.atm.util.DatabaseConnection;

import java.sql.*;

/**
 * Data Access Object for login attempt tracking.
 */
public class LoginAttemptDAO {

    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();

    /**
     * Records a login attempt (ATM or Admin).
     */
    public void recordAttempt(String cardNumber, String adminUsername, String attemptType,
                              boolean success, String failureReason) throws SQLException {
        String sql = "INSERT INTO login_attempts (card_number, admin_username, attempt_type, success, failure_reason) " +
                     "VALUES (?, ?, ?, ?, ?)";

        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            stmt.setString(2, adminUsername);
            stmt.setString(3, attemptType);
            stmt.setBoolean(4, success);
            stmt.setString(5, failureReason);
            stmt.executeUpdate();
        } finally {
            dbPool.releaseConnection(conn);
        }
    }

    /**
     * Records an ATM card login attempt.
     */
    public void recordATMAttempt(String cardNumber, boolean success, String failureReason) throws SQLException {
        recordAttempt(cardNumber, null, "ATM", success, failureReason);
    }

    /**
     * Records an admin login attempt.
     */
    public void recordAdminAttempt(String username, boolean success, String failureReason) throws SQLException {
        recordAttempt(null, username, "ADMIN", success, failureReason);
    }
}
