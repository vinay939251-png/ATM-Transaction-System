package com.atm.dao;

import com.atm.model.Account;
import com.atm.model.Customer;
import com.atm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Account operations.
 * Uses prepared statements throughout.
 */
public class AccountDAO {

    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();

    /**
     * Finds an account by account ID.
     */
    public Account findById(int accountId) throws SQLException {
        String sql = "SELECT a.*, c.first_name, c.last_name, c.email, c.phone " +
                     "FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.customer_id " +
                     "WHERE a.account_id = ?";

        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapAccountWithCustomer(rs);
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds an account by account number.
     */
    public Account findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT a.*, c.first_name, c.last_name, c.email, c.phone " +
                     "FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.customer_id " +
                     "WHERE a.account_number = ?";

        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapAccountWithCustomer(rs);
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Gets the current balance directly from the database.
     * Does NOT rely on cached/in-memory values.
     */
    public BigDecimal getBalance(int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("balance");
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Atomically debits an account — uses conditional UPDATE to prevent overdraft.
     * Returns the number of rows affected (1 = success, 0 = insufficient funds).
     * This method uses the connection provided by the caller for transaction control.
     */
    public int debit(int accountId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance - ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE account_id = ? AND status = 'ACTIVE' AND (balance - ?) >= 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, accountId);
            stmt.setBigDecimal(3, amount);
            return stmt.executeUpdate();
        }
    }

    /**
     * Credits an account. Uses the connection provided by the caller.
     */
    public int credit(int accountId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE account_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, accountId);
            return stmt.executeUpdate();
        }
    }

    /**
     * Lists all accounts (for admin).
     */
    public List<Account> findAll() throws SQLException {
        String sql = "SELECT a.*, c.first_name, c.last_name, c.email, c.phone " +
                     "FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.customer_id " +
                     "ORDER BY a.account_id";
        List<Account> accounts = new ArrayList<>();
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapAccountWithCustomer(rs));
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return accounts;
    }

    /**
     * Lists all accounts for a specific customer.
     */
    public List<Account> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT a.*, c.first_name, c.last_name, c.email, c.phone " +
                     "FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.customer_id " +
                     "WHERE a.customer_id = ? ORDER BY a.account_id";
        List<Account> accounts = new ArrayList<>();
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapAccountWithCustomer(rs));
                }
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return accounts;
    }

    /**
     * Checks if an account number already exists.
     */
    public boolean existsByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT 1 FROM accounts WHERE account_number = ? LIMIT 1";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
    }

    /**
     * Inserts a new account record within an existing database transaction.
     * Returns the generated account_id.
     */
    public int insert(Account acc, Connection conn) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance, min_balance, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, acc.getAccountNumber());
            stmt.setInt(2, acc.getCustomerId());
            stmt.setString(3, acc.getAccountType() != null ? acc.getAccountType().name() : "SAVINGS");
            stmt.setBigDecimal(4, acc.getBalance());
            stmt.setBigDecimal(5, acc.getMinBalance() != null ? acc.getMinBalance() : new BigDecimal("1000.00"));
            stmt.setString(6, acc.getStatus() != null ? acc.getStatus().name() : "ACTIVE");

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    acc.setAccountId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to retrieve generated account ID.");
    }

    private Account mapAccountWithCustomer(ResultSet rs) throws SQLException {
        Account acc = new Account();
        acc.setAccountId(rs.getInt("account_id"));
        acc.setAccountNumber(rs.getString("account_number"));
        acc.setCustomerId(rs.getInt("customer_id"));
        acc.setAccountType(Account.AccountType.valueOf(rs.getString("account_type")));
        acc.setBalance(rs.getBigDecimal("balance"));
        acc.setMinBalance(rs.getBigDecimal("min_balance"));
        acc.setStatus(Account.AccountStatus.valueOf(rs.getString("status")));
        acc.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        acc.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        acc.setCustomer(customer);

        return acc;
    }
}
