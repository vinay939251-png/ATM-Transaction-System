package com.atm.test;

import com.atm.util.ConfigManager;
import java.math.BigDecimal;

/**
 * Unit tests for ConfigManager properties loader.
 */
public class ConfigManagerTest {

    public static void runTests(TestReporter reporter) {
        reporter.section("ConfigManager Tests");

        ConfigManager config = ConfigManager.getInstance();

        reporter.test("ConfigManager loads singleton instance", () -> {
            reporter.assertTrue(config != null, "ConfigManager instance should not be null");
            ConfigManager another = ConfigManager.getInstance();
            reporter.assertEquals(config, another, "ConfigManager should be a singleton");
        });

        reporter.test("ConfigManager retrieves withdrawal limits", () -> {
            BigDecimal maxTxn = config.getMaxWithdrawalPerTransaction();
            reporter.assertTrue(maxTxn != null, "Max per txn limit must exist");
            reporter.assertTrue(maxTxn.compareTo(BigDecimal.ZERO) > 0, "Max per txn limit must be positive");
            reporter.assertEquals(new BigDecimal("25000.00"), maxTxn, "Default max per txn should be 25000.00");

            BigDecimal maxDaily = config.getMaxDailyWithdrawal();
            reporter.assertTrue(maxDaily != null, "Max daily limit must exist");
            reporter.assertTrue(maxDaily.compareTo(maxTxn) >= 0, "Daily limit should be >= per txn limit");
        });

        reporter.test("ConfigManager retrieves security thresholds", () -> {
            int maxAttempts = config.getMaxLoginAttempts();
            reporter.assertEquals(3, maxAttempts, "Max login attempts should default to 3");

            int blockMinutes = config.getBlockDurationMinutes();
            reporter.assertEquals(30, blockMinutes, "Block duration should default to 30 minutes");
        });

        reporter.test("ConfigManager returns default for nonexistent key", () -> {
            String fallback = config.get("nonexistent.key", "default_val");
            reporter.assertEquals("default_val", fallback, "Fallback value should be returned");
        });
    }
}
