package com.atm.test;

import com.atm.util.PasswordHasher;

/**
 * Unit tests for BCrypt password and PIN hashing utility.
 */
public class PasswordHasherTest {

    public static void runTests(TestReporter reporter) {
        reporter.section("PasswordHasher Tests");

        // Test 1: Successful hashing and verification
        reporter.test("BCrypt hash and verify valid PIN", () -> {
            String pin = "1234";
            String hash = PasswordHasher.hash(pin);
            reporter.assertTrue(hash != null && hash.startsWith("$2a$12$"), "Hash should start with $2a$12$");
            reporter.assertTrue(PasswordHasher.verify(pin, hash), "Verify should succeed for correct PIN");
        });

        // Test 2: Invalid PIN verification failure
        reporter.test("BCrypt verify rejects incorrect PIN", () -> {
            String pin = "1234";
            String hash = PasswordHasher.hash(pin);
            reporter.assertFalse(PasswordHasher.verify("9999", hash), "Verify should fail for incorrect PIN");
            reporter.assertFalse(PasswordHasher.verify("1235", hash), "Verify should fail for off-by-one PIN");
        });

        // Test 3: Salt uniqueness (same PIN produces distinct hashes)
        reporter.test("BCrypt produces unique salt for identical PINs", () -> {
            String hash1 = PasswordHasher.hash("1234");
            String hash2 = PasswordHasher.hash("1234");
            reporter.assertFalse(hash1.equals(hash2), "Hashes should differ due to random salting");
            reporter.assertTrue(PasswordHasher.verify("1234", hash1), "Hash 1 should verify");
            reporter.assertTrue(PasswordHasher.verify("1234", hash2), "Hash 2 should verify");
        });

        // Test 4: Null and empty handling
        reporter.test("BCrypt handles null and empty inputs safely", () -> {
            reporter.assertFalse(PasswordHasher.verify(null, "$2a$12$..."), "Null input should return false");
            reporter.assertFalse(PasswordHasher.verify("1234", null), "Null hash should return false");
            
            boolean exceptionThrown = false;
            try {
                PasswordHasher.hash(null);
            } catch (IllegalArgumentException e) {
                exceptionThrown = true;
            }
            reporter.assertTrue(exceptionThrown, "Hashing null should throw IllegalArgumentException");
        });

        // Test 5: Verify sample data PINs
        reporter.test("Sample dataset BCrypt hashes verify correctly", () -> {
            String h1234 = "$2a$12$NswJp0p8g7nSagMizknac.ItdeNyKEVAeyTWlVsWxjsGLR9uXrd3y";
            String hAdmin = "$2a$12$5crfbcYFJIvCcSAB5ec5uuhHKWlv0b28aub8T3PkQen/fpk7pC24q";
            reporter.assertTrue(PasswordHasher.verify("1234", h1234), "Sample PIN 1234 should verify");
            reporter.assertTrue(PasswordHasher.verify("admin123", hAdmin), "Sample Admin password should verify");
        });
    }
}
