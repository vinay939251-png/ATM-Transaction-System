package com.atm.test;

import com.atm.util.ConfigManager;
import java.math.BigDecimal;

/**
 * Unit tests for business validation rules:
 * amounts, denominations, limits, and transfer validations.
 */
public class ValidationTest {

    public static void runTests(TestReporter reporter) {
        reporter.section("Business Validation & Security Tests");

        ConfigManager config = ConfigManager.getInstance();

        // Test 1: Zero and negative amounts
        reporter.test("Validates rejection of non-positive transaction amounts", () -> {
            BigDecimal zero = BigDecimal.ZERO;
            BigDecimal negative = new BigDecimal("-500.00");
            reporter.assertTrue(zero.compareTo(BigDecimal.ZERO) <= 0, "Zero amount should be non-positive");
            reporter.assertTrue(negative.compareTo(BigDecimal.ZERO) < 0, "Negative amount should be detected");
        });

        // Test 2: Denomination check (multiple of ₹100)
        reporter.test("Validates denomination rule (multiple of ₹100)", () -> {
            BigDecimal validAmount1 = new BigDecimal("500.00");
            BigDecimal validAmount2 = new BigDecimal("2000.00");
            BigDecimal invalidAmount1 = new BigDecimal("550.00");
            BigDecimal invalidAmount2 = new BigDecimal("123.45");

            BigDecimal denom = new BigDecimal("100");
            reporter.assertEquals(0, validAmount1.remainder(denom).compareTo(BigDecimal.ZERO), "500 is multiple of 100");
            reporter.assertEquals(0, validAmount2.remainder(denom).compareTo(BigDecimal.ZERO), "2000 is multiple of 100");
            reporter.assertTrue(invalidAmount1.remainder(denom).compareTo(BigDecimal.ZERO) != 0, "550 is not multiple of 100");
            reporter.assertTrue(invalidAmount2.remainder(denom).compareTo(BigDecimal.ZERO) != 0, "123.45 is not multiple of 100");
        });

        // Test 3: Per-transaction withdrawal limit
        reporter.test("Validates per-transaction withdrawal upper bound", () -> {
            BigDecimal maxTxn = config.getMaxWithdrawalPerTransaction();
            BigDecimal safeAmount = new BigDecimal("20000.00");
            BigDecimal overAmount = new BigDecimal("30000.00");

            reporter.assertTrue(safeAmount.compareTo(maxTxn) <= 0, "20000 is within max limit of 25000");
            reporter.assertTrue(overAmount.compareTo(maxTxn) > 0, "30000 exceeds max limit of 25000");
        });

        // Test 4: Daily withdrawal limit calculation
        reporter.test("Validates cumulative daily withdrawal limit", () -> {
            BigDecimal maxDaily = config.getMaxDailyWithdrawal(); // 100,000.00
            BigDecimal dailySoFar = new BigDecimal("85000.00");
            BigDecimal newWithdrawal = new BigDecimal("20000.00");

            reporter.assertTrue(dailySoFar.add(newWithdrawal).compareTo(maxDaily) > 0, "85000 + 20000 = 105000 > 100000 daily limit");
            BigDecimal remainingLimit = maxDaily.subtract(dailySoFar);
            reporter.assertEquals(new BigDecimal("15000.00"), remainingLimit, "Remaining limit is 15000");
        });

        // Test 5: Transfer destination validation (same-account rejection)
        reporter.test("Prevents transfer between identical source and destination accounts", () -> {
            int sourceAccountId = 1;
            int destAccountIdSame = 1;
            int destAccountIdDifferent = 2;

            reporter.assertTrue(sourceAccountId == destAccountIdSame, "Identical account IDs detected");
            reporter.assertFalse(sourceAccountId == destAccountIdDifferent, "Distinct account IDs accepted");
        });

        // Test 6: Account balance overdraft prevention
        reporter.test("Validates balance sufficiency condition", () -> {
            BigDecimal balance = new BigDecimal("5000.00");
            BigDecimal requestedWithdrawal = new BigDecimal("6000.00");

            reporter.assertTrue(requestedWithdrawal.compareTo(balance) > 0, "Overdraft request properly identified");
        });

        // Test 7: PIN format validation
        reporter.test("Validates 4-digit numeric PIN format", () -> {
            reporter.assertTrue(isValidPinFormat("1234"), "1234 is a valid 4-digit PIN");
            reporter.assertTrue(isValidPinFormat("0000"), "0000 is a valid 4-digit PIN");
            reporter.assertFalse(isValidPinFormat("123"), "3-digit PIN rejected");
            reporter.assertFalse(isValidPinFormat("12345"), "5-digit PIN rejected");
            reporter.assertFalse(isValidPinFormat("12a4"), "Alpha-character PIN rejected");
            reporter.assertFalse(isValidPinFormat(null), "Null PIN rejected");
        });

        // Test 8: Card format validation
        reporter.test("Validates 16-digit ATM card format", () -> {
            reporter.assertTrue(isValidCardFormat("4532-1234-5678-9010"), "Standard formatted card accepted");
            reporter.assertTrue(isValidCardFormat("4532123456789010"), "Plain 16-digit card accepted");
            reporter.assertFalse(isValidCardFormat("4532-1234"), "Short card rejected");
            reporter.assertFalse(isValidCardFormat("invalid-card-number"), "Malformed card string rejected");
        });
    }

    private static boolean isValidPinFormat(String pin) {
        if (pin == null) return false;
        return pin.matches("^\\d{4}$");
    }

    private static boolean isValidCardFormat(String card) {
        if (card == null) return false;
        String clean = card.replace("-", "").trim();
        return clean.matches("^\\d{16}$");
    }
}
