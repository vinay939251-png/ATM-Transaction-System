package com.atm.test;

import com.atm.model.Account;
import com.atm.service.AuthenticationService;
import com.atm.service.RegistrationService;
import com.atm.service.RegistrationService.RegistrationRequest;
import com.atm.service.RegistrationService.RegistrationResult;

import java.math.BigDecimal;

/**
 * Automated tests for customer sign-up, account opening,
 * and custom/auto-generated ATM card issuance.
 */
public class RegistrationServiceTest {

    public static void runTests(TestReporter reporter) {
        reporter.section("Registration & Sign-Up Tests");

        RegistrationService service = new RegistrationService();

        // Test 1: Card number formatting
        reporter.test("Card number formatting helper formats 16 raw digits", () -> {
            String raw = "4532123456789010";
            String formatted = RegistrationService.formatCardNumber(raw);
            reporter.assertEquals("4532-1234-5678-9010", formatted, "Raw 16 digits formatted with hyphens");
        });

        // Test 2: Validation rejection on empty first name
        reporter.test("Rejects sign-up request with missing first name", () -> {
            RegistrationRequest req = new RegistrationRequest(
                    "", "Doe", "test@example.com", "9876543210",
                    Account.AccountType.SAVINGS, new BigDecimal("2000.00"),
                    "4532-9999-8888-7777", "1234"
            );
            RegistrationResult res = service.register(req);
            reporter.assertFalse(res.success(), "Should fail on empty first name");
        });

        // Test 3: Validation rejection on deposit below minimum balance
        reporter.test("Rejects sign-up with deposit below required minimum", () -> {
            RegistrationRequest req = new RegistrationRequest(
                    "Test", "User", "test.minbal@example.com", "9876543210",
                    Account.AccountType.CURRENT, new BigDecimal("2000.00"), // Current requires 5000 min
                    "4532-9999-8888-7777", "1234"
            );
            RegistrationResult res = service.register(req);
            reporter.assertFalse(res.success(), "Should fail when initial deposit < 5000 for CURRENT");
        });

        // Test 4: Validation rejection on invalid PIN format
        reporter.test("Rejects sign-up with non-4-digit PIN", () -> {
            RegistrationRequest req = new RegistrationRequest(
                    "Test", "User", "test.pin@example.com", "9876543210",
                    Account.AccountType.SAVINGS, new BigDecimal("2000.00"),
                    "4532-9999-8888-7777", "12" // only 2 digits
            );
            RegistrationResult res = service.register(req);
            reporter.assertFalse(res.success(), "Should fail on 2-digit PIN");
        });

        // Test 5: Unique card generator produces valid format
        reporter.test("Card number generator produces standard 4532 prefix format", () -> {
            String genCard = service.generateUniqueCardNumber();
            reporter.assertTrue(genCard != null && genCard.startsWith("4532-"), "Card starts with 4532-");
            reporter.assertEquals(19, genCard.length(), "Card number with hyphens is 19 characters");
        });

        // Test 6: Live sign-up with custom card number and authentication verification
        reporter.test("Live sign-up provisions account and authenticates with new PIN", () -> {
            long randSuffix = System.currentTimeMillis() % 100000;
            String customCard = "4532-7777-6666-" + String.format("%04d", randSuffix % 10000);
            String testEmail = "signup.test." + randSuffix + "@example.com";
            String testPhone = "98" + String.format("%08d", randSuffix);
            String newPin = "4321";

            RegistrationRequest req = new RegistrationRequest(
                    "Vinay", "Custom", testEmail, testPhone,
                    Account.AccountType.SAVINGS, new BigDecimal("15000.00"),
                    customCard, newPin
            );

            RegistrationResult res = service.register(req);
            reporter.assertTrue(res.success(), "Registration should succeed: " + res.message());
            reporter.assertEquals(customCard, res.cardNumber(), "Returned card number matches custom card");
            reporter.assertTrue(res.accountNumber() != null && res.accountNumber().startsWith("ACC"), "Account number generated");

            // Verify the newly created user can log in immediately
            AuthenticationService auth = new AuthenticationService();
            var authRes = auth.authenticateATMUser(customCard, newPin);
            reporter.assertTrue(authRes.success(), "New user should authenticate successfully with PIN " + newPin);
        });
    }
}
