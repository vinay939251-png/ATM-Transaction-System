package com.atm.dao;

import com.atm.model.Admin;
import com.atm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Admin operations.
 */
public class AdminDAO {

    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();

    /**
     * Finds an admin by username.
     */
    public Admin findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ?";
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapAdmin(rs);
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Lists all admins.
     */
    public List<Admin> findAll() throws SQLException {
        String sql = "SELECT * FROM admins ORDER BY admin_id";
        List<Admin> admins = new ArrayList<>();
        Connection conn = dbPool.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                admins.add(mapAdmin(rs));
            }
        } finally {
            dbPool.releaseConnection(conn);
        }
        return admins;
    }

    private Admin mapAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setUsername(rs.getString("username"));
        admin.setPasswordHash(rs.getString("password_hash"));
        admin.setFullName(rs.getString("full_name"));
        admin.setRole(Admin.AdminRole.valueOf(rs.getString("role")));
        admin.setActive(rs.getBoolean("is_active"));
        admin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        admin.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return admin;
    }
}
