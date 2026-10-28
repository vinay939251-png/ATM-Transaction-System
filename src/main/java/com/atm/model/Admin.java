package com.atm.model;

import java.time.LocalDateTime;

/**
 * Represents an administrator user with separate authentication.
 */
public class Admin {

    public enum AdminRole { SUPER_ADMIN, ADMIN }

    private int adminId;
    private String username;
    private String passwordHash;     // BCrypt hash — never expose
    private String fullName;
    private AdminRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Admin() {
        this.role = AdminRole.ADMIN;
        this.active = true;
    }

    // Getters and Setters
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public AdminRole getRole() { return role; }
    public void setRole(AdminRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("Admin[%s: %s, Role: %s]", username, fullName, role);
    }
}
