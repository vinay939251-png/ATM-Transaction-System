package com.atm.test;

import com.atm.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Unit tests for domain model entities and business logic methods.
 */
public class ModelTest {

    public static void runTests(TestReporter reporter) {
        reporter.section("Domain Model Tests");

        // Test 1: ATMCard masking and properties
        reporter.test("ATMCard masks card number correctly for security", () -> {
            ATMCard card = new ATMCard();
            card.setCardNumber("4532-1234-5678-9010");
            reporter.assertEquals("****-****-****-9010", card.getMaskedCardNumber(), "Card number should be masked");

            card.setCardNumber("1234");
            reporter.assertEquals("****-****-****-1234", card.getMaskedCardNumber(), "Short card masked");
        });

        // Test 2: ATMCard status transitions
        reporter.test("ATMCard tracks status and block transitions", () -> {
            ATMCard card = new ATMCard();
            reporter.assertEquals(ATMCard.CardStatus.ACTIVE, card.getCardStatus(), "Initial status should be ACTIVE");
            reporter.assertEquals(0, card.getFailedAttempts(), "Initial failed attempts should be 0");

            card.setCardStatus(ATMCard.CardStatus.BLOCKED);
            LocalDateTime unblockTime = LocalDateTime.now().plusMinutes(30);
            card.setBlockedUntil(unblockTime);
            reporter.assertEquals(ATMCard.CardStatus.BLOCKED, card.getCardStatus(), "Status should be BLOCKED");
            reporter.assertEquals(unblockTime, card.getBlockedUntil(), "BlockedUntil timestamp matches");
        });

        // Test 3: Account balances and status
        reporter.test("Account entity models balance and minimum requirements", () -> {
            Account account = new Account();
            account.setAccountId(1);
            account.setAccountNumber("ACC1001000001");
            account.setAccountType(Account.AccountType.SAVINGS);
            account.setBalance(new BigDecimal("75000.50"));
            account.setMinBalance(new BigDecimal("1000.00"));
            account.setStatus(Account.AccountStatus.ACTIVE);

            reporter.assertEquals("ACC1001000001", account.getAccountNumber(), "Account number matches");
            reporter.assertEquals(new BigDecimal("75000.50"), account.getBalance(), "Balance matches");
            reporter.assertEquals(Account.AccountStatus.ACTIVE, account.getStatus(), "Status matches");
        });

        // Test 4: Customer entity
        reporter.test("Customer entity formats full name and contact info", () -> {
            Customer customer = new Customer();
            customer.setCustomerId(1);
            customer.setFirstName("Aarav");
            customer.setLastName("Sharma");
            customer.setEmail("aarav.sharma@example.com");
            customer.setPhone("9876543210");

            reporter.assertEquals("Aarav Sharma", customer.getFullName(), "Full name formatting matches");
            reporter.assertEquals("aarav.sharma@example.com", customer.getEmail(), "Email matches");
        });

        // Test 5: Transaction entity types and status
        reporter.test("Transaction entity records transaction details and audit fields", () -> {
            Transaction txn = new Transaction();
            txn.setTransactionId(101);
            txn.setAccountId(1);
            txn.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
            txn.setAmount(new BigDecimal("5000.00"));
            txn.setBalanceAfter(new BigDecimal("70000.00"));
            txn.setStatus(Transaction.TransactionStatus.SUCCESS);

            reporter.assertEquals(Transaction.TransactionType.WITHDRAWAL, txn.getTransactionType(), "Type matches");
            reporter.assertEquals(new BigDecimal("5000.00"), txn.getAmount(), "Amount matches");
            reporter.assertEquals(new BigDecimal("70000.00"), txn.getBalanceAfter(), "Balance after matches");
        });

        // Test 6: Transfer entity relationships
        reporter.test("Transfer entity maintains source, destination and amount", () -> {
            Transfer transfer = new Transfer();
            transfer.setTransferId(201);
            transfer.setSourceAccountId(1);
            transfer.setDestAccountId(3);
            transfer.setAmount(new BigDecimal("2500.00"));
            transfer.setStatus(Transfer.TransferStatus.SUCCESS);

            reporter.assertEquals(1, transfer.getSourceAccountId(), "Source account matches");
            reporter.assertEquals(3, transfer.getDestAccountId(), "Destination account matches");
            reporter.assertEquals(new BigDecimal("2500.00"), transfer.getAmount(), "Amount matches");
        });

        // Test 7: Admin entity roles and status
        reporter.test("Admin entity handles role and active state", () -> {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setRole(Admin.AdminRole.SUPER_ADMIN);
            admin.setActive(true);

            reporter.assertEquals("admin", admin.getUsername(), "Username matches");
            reporter.assertEquals(Admin.AdminRole.SUPER_ADMIN, admin.getRole(), "Role matches");
            reporter.assertTrue(admin.isActive(), "Admin should be active");
        });
    }
}
