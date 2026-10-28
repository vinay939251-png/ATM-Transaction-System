package com.atm.dao;

import com.atm.model.ATMCard;
import com.atm.model.Account;
import com.atm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ATM Card operations.
 * All queries use prepared statements to prevent SQL injection.
 */
public class ATMCardDAO {

    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();

    /**
     * Finds an ATM card by card number.
     * Intentionally does not reveal whether the card exists to unauthenticated callers.
     */
    public ATMCard findByCardNumber(String cardNumber) throws SQLException {
        String sql = "SELECT c.*, a.account_number, a.balance, a.account_type, a.status AS account_status, " +
                     "a.customer_id, a.min_balance " +
                     "FROM atm_cards c " +
                     "JOIN accounts a ON c.account_id = a.account_id " +
                     "WHERE c.card_number = ?";

        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCardWithAccount(rs);
                }
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Increments failed login attempts for a card.
     */
    public void incrementFailedAttempts(int cardId) throws SQLException {
        String sql = "UPDATE atm_cards SET failed_attempts = failed_attempts + 1, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE card_id = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cardId);
            stmt.executeUpdate();
        } finally {
            dbPool.releaseConnection(conn);
        }
    }

    /**
     * Blocks a card and sets the blocked_until timestamp.
     */
    public void blockCard(int cardId, LocalDateTime blockedUntil) throws SQLException {
        String sql = "UPDATE atm_cards SET card_status = 'BLOCKED', blocked_until = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE card_id = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(blockedUntil));
            stmt.setInt(2, cardId);
            stmt.executeUpdate();
        } finally {
            dbPool.releaseConnection(conn);
        }
    }

    /**
     * Resets failed attempts after a successful login.
     */
    public void resetFailedAttempts(int cardId) throws SQLException {
        String sql = "UPDATE atm_cards SET failed_attempts = 0, blocked_until = NULL, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE card_id = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cardId);
            stmt.executeUpdate();
        } finally {
            dbPool.releaseConnection(conn);
        }
    }

    /**
     * Activates a blocked card (admin operation).
     */
    public void activateCard(int cardId) throws SQLException {
        String sql = "UPDATE atm_cards SET card_status = 'ACTIVE', failed_attempts = 0, blocked_until = NULL, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE card_id = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cardId);
            stmt.executeUpdate();
        } finally {
            dbPool.releaseConnection(conn);
        }
    }

    /**
     * Updates daily withdrawal tracking for a card.
     */
    public void updateDailyWithdrawal(int cardId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE atm_cards SET " +
                     "daily_withdrawal_total = CASE " +
                     "  WHEN last_withdrawal_date = CURDATE() THEN daily_withdrawal_total + ? " +
                     "  ELSE ? END, " +
                     "last_withdrawal_date = CURDATE(), " +
                     "updated_at = CURRENT_TIMESTAMP " +
                     "WHERE card_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setBigDecimal(2, amount);
            stmt.setInt(3, cardId);
            stmt.executeUpdate();
        }
    }

    /**
     * Gets the daily withdrawal total for a card (resets if it's a new day).
     */
    public BigDecimal getDailyWithdrawalTotal(int cardId) throws SQLException {
        String sql = "SELECT daily_withdrawal_total, last_withdrawal_date FROM atm_cards WHERE card_id = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cardId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date lastDate = rs.getDate("last_withdrawal_date");
                    if (lastDate != null && lastDate.toLocalDate().equals(LocalDate.now())) {
                        return rs.getBigDecimal("daily_withdrawal_total");
                    }
                }
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lists all ATM cards (for admin).
     */
    public List<ATMCard> findAll() throws SQLException {
        String sql = "SELECT c.*, a.account_number, a.balance, a.account_type, a.status AS account_status, " +
                     "a.customer_id, a.min_balance " +
                     "FROM atm_cards c " +
                     "JOIN accounts a ON c.account_id = a.account_id " +
                     "ORDER BY c.card_id";
        List<ATMCard> cards = new ArrayList<>();
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cards.add(mapCardWithAccount(rs));
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return cards;
    }

    /**
     * Checks if a card number already exists in the system.
     */
    public boolean existsByCardNumber(String cardNumber) throws SQLException {
        String sql = "SELECT 1 FROM atm_cards WHERE card_number = ? LIMIT 1";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
    }

    /**
     * Inserts a new ATM card record within an existing database transaction.
     * Returns the generated card_id.
     */
    public int insert(ATMCard card, Connection conn) throws SQLException {
        String sql = "INSERT INTO atm_cards (card_number, account_id, pin_hash, card_status, expiry_date, issued_date, failed_attempts) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, card.getCardNumber());
            stmt.setInt(2, card.getAccountId());
            stmt.setString(3, card.getPinHash());
            stmt.setString(4, card.getCardStatus() != null ? card.getCardStatus().name() : "ACTIVE");
            stmt.setDate(5, Date.valueOf(card.getExpiryDate() != null ? card.getExpiryDate() : LocalDate.now().plusYears(5)));
            stmt.setDate(6, Date.valueOf(card.getIssuedDate() != null ? card.getIssuedDate() : LocalDate.now()));
            stmt.setInt(7, card.getFailedAttempts());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    card.setCardId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to retrieve generated card ID.");
    }

    private ATMCard mapCardWithAccount(ResultSet rs) throws SQLException {
        ATMCard card = new ATMCard();
        card.setCardId(rs.getInt("card_id"));
        card.setCardNumber(rs.getString("card_number"));
        card.setAccountId(rs.getInt("account_id"));
        card.setPinHash(rs.getString("pin_hash"));
        card.setCardStatus(ATMCard.CardStatus.valueOf(rs.getString("card_status")));
        card.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        card.setIssuedDate(rs.getDate("issued_date").toLocalDate());
        card.setFailedAttempts(rs.getInt("failed_attempts"));

        Timestamp blockedUntil = rs.getTimestamp("blocked_until");
        card.setBlockedUntil(blockedUntil != null ? blockedUntil.toLocalDateTime() : null);

        card.setDailyWithdrawalTotal(rs.getBigDecimal("daily_withdrawal_total"));
        Date lastWd = rs.getDate("last_withdrawal_date");
        card.setLastWithdrawalDate(lastWd != null ? lastWd.toLocalDate() : null);

        // Map associated account
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setAccountType(Account.AccountType.valueOf(rs.getString("account_type")));
        account.setStatus(Account.AccountStatus.valueOf(rs.getString("account_status")));
        account.setCustomerId(rs.getInt("customer_id"));
        account.setMinBalance(rs.getBigDecimal("min_balance"));
        card.setAccount(account);

        return card;
    }
}
