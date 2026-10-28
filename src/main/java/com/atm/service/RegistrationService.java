package com.atm.service;

import com.atm.dao.ATMCardDAO;
import com.atm.dao.AccountDAO;
import com.atm.dao.CustomerDAO;
import com.atm.dao.TransactionDAO;
import com.atm.model.ATMCard;
import com.atm.model.Account;
import com.atm.model.Customer;
import com.atm.model.Transaction;
import com.atm.util.DatabaseConnection;
import com.atm.util.PasswordHasher;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

/**
 * Service orchestrating customer sign-up, bank account opening,
 * and custom/auto-generated ATM card issuance.
 * Enforces atomic database transactions and BCrypt credential protection.
 */
public class RegistrationService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final ATMCardDAO cardDAO = new ATMCardDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DatabaseConnection dbPool = DatabaseConnection.getInstance();
    private final Random random = new Random();

    public record RegistrationRequest(
            String firstName,
            String lastName,
            String email,
            String phone,
            Account.AccountType accountType,
            BigDecimal initialDeposit,
            String cardNumber, // custom card number or auto-generated
            String pin
    ) {}

    public record RegistrationResult(
            boolean success,
            String message,
            String cardNumber,
            String accountNumber,
            ATMCard card
    ) {
        public static RegistrationResult success(String msg, String cardNum, String accNum, ATMCard card) {
            return new RegistrationResult(true, msg, cardNum, accNum, card);
        }
        public static RegistrationResult failure(String msg) {
            return new RegistrationResult(false, msg, null, null, null);
        }
    }

    /**
     * Registers a new customer, provisions an account, and issues an ATM card.
     */
    public RegistrationResult register(RegistrationRequest req) {
        // --- 1. Validation ---
        if (req.firstName() == null || req.firstName().trim().isEmpty()) {
            return RegistrationResult.failure("First name is required.");
        }
        if (req.lastName() == null || req.lastName().trim().isEmpty()) {
            return RegistrationResult.failure("Last name is required.");
        }
        if (req.email() == null || !req.email().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return RegistrationResult.failure("Please enter a valid email address.");
        }
        String cleanPhone = req.phone() != null ? req.phone().replaceAll("[^0-9]", "") : "";
        if (cleanPhone.length() < 10) {
            return RegistrationResult.failure("Phone number must contain at least 10 digits.");
        }

        // Validate initial deposit vs minimum required balance
        BigDecimal minBalance = (req.accountType() == Account.AccountType.CURRENT)
                ? new BigDecimal("5000.00")
                : new BigDecimal("1000.00");

        BigDecimal deposit = req.initialDeposit() != null ? req.initialDeposit() : minBalance;
        if (deposit.compareTo(minBalance) < 0) {
            return RegistrationResult.failure("Minimum opening balance for " + req.accountType() +
                    " account is ₹" + minBalance.toPlainString());
        }

        // Validate & format card number
        String formattedCardNumber;
        if (req.cardNumber() == null || req.cardNumber().trim().isEmpty()) {
            formattedCardNumber = generateUniqueCardNumber();
        } else {
            String rawCard = req.cardNumber().trim().replaceAll("[^0-9]", "");
            if (rawCard.length() != 16) {
                return RegistrationResult.failure("Card number must contain exactly 16 digits.");
            }
            formattedCardNumber = formatCardNumber(rawCard);
        }

        // Validate PIN
        if (req.pin() == null || !req.pin().trim().matches("^\\d{4}$")) {
            return RegistrationResult.failure("PIN must be exactly 4 numeric digits.");
        }

        // --- 2. Check for Duplicates in DB ---
        try {
            if (cardDAO.existsByCardNumber(formattedCardNumber)) {
                return RegistrationResult.failure("Card number " + formattedCardNumber +
                        " is already in use. Please enter a different number or generate one.");
            }
            if (customerDAO.findByEmail(req.email().trim()) != null) {
                return RegistrationResult.failure("A customer with this email address already exists.");
            }
            if (customerDAO.findByPhone(cleanPhone) != null) {
                return RegistrationResult.failure("A customer with this phone number already exists.");
            }
        } catch (SQLException e) {
            return RegistrationResult.failure("We couldn't reach the account service. Check that MySQL is running and try again.");
        }

        // --- 3. Atomic Database Transaction ---
        Connection conn = null;
        try {
            conn = dbPool.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // A. Create Customer
            Customer customer = new Customer();
            customer.setFirstName(req.firstName().trim());
            customer.setLastName(req.lastName().trim());
            customer.setEmail(req.email().trim());
            customer.setPhone(cleanPhone);
            customer.setDateOfBirth(LocalDate.of(2000, 1, 1));
            customer.setGender("O");
            customer.setAddress("Customer Registered Address");
            customer.setCity("Hyderabad");
            customer.setState("Telangana");
            customer.setZipCode("500001");

            int customerId = customerDAO.insert(customer, conn);

            // B. Create Account
            String accountNumber = generateUniqueAccountNumber();
            Account account = new Account();
            account.setAccountNumber(accountNumber);
            account.setCustomerId(customerId);
            account.setAccountType(req.accountType() != null ? req.accountType() : Account.AccountType.SAVINGS);
            account.setBalance(deposit);
            account.setMinBalance(minBalance);
            account.setStatus(Account.AccountStatus.ACTIVE);

            int accountId = accountDAO.insert(account, conn);

            // C. Create ATM Card with BCrypt Hashed PIN
            String pinHash = PasswordHasher.hash(req.pin().trim());
            ATMCard card = new ATMCard();
            card.setCardNumber(formattedCardNumber);
            card.setAccountId(accountId);
            card.setPinHash(pinHash);
            card.setCardStatus(ATMCard.CardStatus.ACTIVE);
            card.setExpiryDate(LocalDate.now().plusYears(5));
            card.setIssuedDate(LocalDate.now());
            card.setFailedAttempts(0);
            card.setAccount(account);

            int cardId = cardDAO.insert(card, conn);
            card.setCardId(cardId);

            // D. Record Initial Deposit Transaction
            if (deposit.compareTo(BigDecimal.ZERO) > 0) {
                Transaction initialTxn = new Transaction();
                initialTxn.setAccountId(accountId);
                initialTxn.setCardId(cardId);
                initialTxn.setTransactionType(Transaction.TransactionType.DEPOSIT);
                initialTxn.setAmount(deposit);
                initialTxn.setBalanceAfter(deposit);
                initialTxn.setStatus(Transaction.TransactionStatus.SUCCESS);
                initialTxn.setDescription("Initial opening deposit");
                transactionDAO.insert(initialTxn, conn);
            }

            // COMMIT TRANSACTION
            conn.commit();

            return RegistrationResult.success(
                    "Account and ATM card created successfully!",
                    formattedCardNumber,
                    accountNumber,
                    card
            );

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[Registration] Transaction rolled back: " + e.getMessage());
            return RegistrationResult.failure("Account creation could not be completed. No changes were saved; please try again.");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                dbPool.releaseConnection(conn);
            }
        }
    }

    /**
     * Formats a 16-digit plain number into XXXX-XXXX-XXXX-XXXX
     */
    public static String formatCardNumber(String raw16Digits) {
        if (raw16Digits == null || raw16Digits.length() != 16) return raw16Digits;
        return raw16Digits.substring(0, 4) + "-" +
               raw16Digits.substring(4, 8) + "-" +
               raw16Digits.substring(8, 12) + "-" +
               raw16Digits.substring(12, 16);
    }

    /**
     * Generates a unique 16-digit card number starting with 4532
     */
    public String generateUniqueCardNumber() {
        while (true) {
            long p2 = 1000 + random.nextInt(9000);
            long p3 = 1000 + random.nextInt(9000);
            long p4 = 1000 + random.nextInt(9000);
            String candidate = "4532-" + p2 + "-" + p3 + "-" + p4;
            try {
                if (!cardDAO.existsByCardNumber(candidate)) {
                    return candidate;
                }
            } catch (SQLException e) {
                return candidate;
            }
        }
    }

    /**
     * Generates a unique account number ACC100XXXXXXX
     */
    private String generateUniqueAccountNumber() {
        while (true) {
            long num = 1000000000L + (Math.abs(random.nextLong()) % 9000000000L);
            String candidate = "ACC" + num;
            try {
                if (!accountDAO.existsByAccountNumber(candidate)) {
                    return candidate;
                }
            } catch (SQLException e) {
                return candidate;
            }
        }
    }
}
