package com.atm;

import com.atm.ui.ATMTheme;
import com.atm.ui.MainFrame;
import com.atm.util.ConfigManager;
import com.atm.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Main application entry point for the ATM Transaction System.
 * Initializes configuration, UI look-and-feel, database connection pool,
 * and launches the main desktop interface.
 */
public class ATMApplication {

    public static void main(String[] args) {
        // High-DPI scaling and rendering enhancements
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize Look and Feel (FlatLaf Dark with fallback)
        setupLookAndFeel();

        // Initialize Config
        ConfigManager.getInstance();

        // Initialize Database connection pool (non-fatal if DB not yet started)
        initializeDatabase();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("[FATAL] Failed to launch ATM UI: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch ATM application:\n" + e.getMessage(),
                        "Launch Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add shutdown hook to close DB pool
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[ATM] Shutting down application...");
            DatabaseConnection.getInstance().shutdown();
        }));
    }

    /**
     * Configures modern Dark Look and Feel using FlatLaf if available,
     * falling back to System or Nimbus.
     */
    private static void setupLookAndFeel() {
        try {
            // Attempt FlatDarkLaf setup via reflection so it builds even without compile-time hard dependency
            Class<?> flatDarkLafClass = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            flatDarkLafClass.getMethod("setup").invoke(null);
            
            // Customize global FlatLaf / Swing UI defaults
            UIManager.put("Panel.background", ATMTheme.BG_PRIMARY);
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            System.out.println("[UI] FlatDarkLaf initialized successfully.");
        } catch (Exception ex) {
            System.out.println("[UI] FlatLaf not found on classpath, falling back to System Look and Feel.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception fallbackEx) {
                // Ignore fallback exception
            }
        }
    }

    /**
     * Initializes the database connection pool.
     * Logs connection state without crashing if MySQL is not currently running.
     */
    private static void initializeDatabase() {
        try {
            DatabaseConnection.getInstance().initialize();
            System.out.println("[DB] Database connection pool initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[DB] WARNING: Could not connect to MySQL database at startup.");
            System.err.println("[DB] Reason: " + e.getMessage());
            System.err.println("[DB] Note: Verify MySQL is running and database schema is imported.");
        }
    }
}
