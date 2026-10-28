package com.atm.dao;

import com.atm.model.Customer;
import com.atm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer operations.
 */
public class CustomerDAO {

    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();

    /**
     * Finds a customer by ID.
     */
    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCustomer(rs);
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Lists all customers (for admin).
     */
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY customer_id";
        List<Customer> customers = new ArrayList<>();
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return customers;
    }

    /**
     * Finds a customer by email.
     */
    public Customer findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCustomer(rs);
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds a customer by phone number.
     */
    public Customer findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCustomer(rs);
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Inserts a new customer record within an existing database transaction.
     * Returns the generated customer_id.
     */
    public int insert(Customer c, Connection conn) throws SQLException {
        String sql = "INSERT INTO customers (first_name, last_name, date_of_birth, gender, email, phone, address, city, state, zip_code) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getFirstName());
            stmt.setString(2, c.getLastName());
            stmt.setDate(3, c.getDateOfBirth() != null ? Date.valueOf(c.getDateOfBirth()) : Date.valueOf("2000-01-01"));
            stmt.setString(4, c.getGender() != null ? c.getGender() : "O");
            stmt.setString(5, c.getEmail());
            stmt.setString(6, c.getPhone());
            stmt.setString(7, c.getAddress() != null ? c.getAddress() : "100 Financial Way");
            stmt.setString(8, c.getCity() != null ? c.getCity() : "City");
            stmt.setString(9, c.getState() != null ? c.getState() : "State");
            stmt.setString(10, c.getZipCode() != null ? c.getZipCode() : "500001");

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    c.setCustomerId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to retrieve generated customer ID.");
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setFirstName(rs.getString("first_name"));
        c.setLastName(rs.getString("last_name"));
        c.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        c.setGender(rs.getString("gender"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setState(rs.getString("state"));
        c.setZipCode(rs.getString("zip_code"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return c;
    }
}
