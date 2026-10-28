package com.atm.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads configuration from config.properties.
 * Falls back to config.example.properties if config.properties is missing.
 * Never logs or exposes credentials.
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final Properties properties;

    private ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig() {
        // Try config.properties first, then config.example.properties
        String[] configFiles = {"config.properties", "config.example.properties"};

        for (String configFile : configFiles) {
            Path path = Paths.get(configFile);
            if (Files.exists(path)) {
                try (InputStream input = new FileInputStream(path.toFile())) {
                    properties.load(input);
                    System.out.println("[Config] Loaded configuration from: " + configFile);
                    return;
                } catch (IOException e) {
                    System.err.println("[Config] Error reading " + configFile + ": " + e.getMessage());
                }
            }
        }

        // Fallback to classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                System.out.println("[Config] Loaded configuration from classpath.");
                return;
            }
        } catch (IOException e) {
            System.err.println("[Config] Error reading classpath config: " + e.getMessage());
        }

        System.err.println("[Config] WARNING: No configuration file found. Using defaults.");
        setDefaults();
    }

    private void setDefaults() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/atm_transaction_system");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("atm.max.login.attempts", "3");
        properties.setProperty("atm.card.block.duration.minutes", "30");
        properties.setProperty("atm.max.withdrawal.per.transaction", "25000.00");
        properties.setProperty("atm.max.daily.withdrawal", "100000.00");
        properties.setProperty("atm.session.timeout.minutes", "5");
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        try {
            String val = properties.getProperty(key);
            return val != null ? new BigDecimal(val) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Convenience getters for common settings
    public String getDbUrl() { return get("db.url"); }
    public String getDbUsername() { return get("db.username"); }
    public String getDbPassword() { return get("db.password"); }

    public int getMaxLoginAttempts() { return getInt("atm.max.login.attempts", 3); }
    public int getBlockDurationMinutes() { return getInt("atm.card.block.duration.minutes", 30); }
    public int getSessionTimeoutMinutes() { return getInt("atm.session.timeout.minutes", 5); }

    public BigDecimal getMaxWithdrawalPerTransaction() {
        return getBigDecimal("atm.max.withdrawal.per.transaction", new BigDecimal("25000.00"));
    }

    public BigDecimal getMaxDailyWithdrawal() {
        return getBigDecimal("atm.max.daily.withdrawal", new BigDecimal("100000.00"));
    }
}
