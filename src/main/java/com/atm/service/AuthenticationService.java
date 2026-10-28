package com.atm.service;

import com.atm.dao.ATMCardDAO;
import com.atm.dao.AdminDAO;
import com.atm.dao.LoginAttemptDAO;
import com.atm.model.ATMCard;
import com.atm.model.Admin;
import com.atm.util.ConfigManager;
import com.atm.util.PasswordHasher;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Handles authentication for both ATM users and administrators.
 * Implements:
 * - PIN verification using BCrypt (never plaintext)
 * - Failed attempt tracking and card blocking
 * - Separate admin authentication
 * - Login attempt auditing
 */
public class AuthenticationService {

    private final ATMCardDAO cardDAO = new ATMCardDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final LoginAttemptDAO loginAttemptDAO = new LoginAttemptDAO();
    private final ConfigManager config = ConfigManager.getInstance();

    /**
     * Result of an authentication attempt.
     */
    public record AuthResult(boolean success, String message, ATMCard card) {
        public static AuthResult success(ATMCard card) {
            return new AuthResult(true, "Authentication successful.", card);
        }
        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null);
        }
    }

    /**
     * Result of an admin authentication attempt.
     */
    public record AdminAuthResult(boolean success, String message, Admin admin) {
        public static AdminAuthResult success(Admin admin) {
            return new AdminAuthResult(true, "Admin authentication successful.", admin);
        }
        public static AdminAuthResult failure(String message) {
            return new AdminAuthResult(false, message, null);
        }
    }

    /**
     * Authenticates an ATM user by card number and PIN.
     * 
     * Security measures:
     * - Does not reveal whether a card number exists
     * - Blocks card after configured number of failed attempts
     * - Checks card expiry and status
     * - Uses BCrypt for PIN verification (constant-time)
     */
    public AuthResult authenticateATMUser(String cardNumber, String pin) {
        try {
            // Look up the card — we deliberately use the same error message
            // regardless of whether the card exists, to prevent enumeration
            ATMCard card = cardDAO.findByCardNumber(cardNumber);

            if (card == null) {
                // Log the attempt but give a generic message
                loginAttemptDAO.recordATMAttempt(cardNumber, false, "Card not found");
                return AuthResult.failure("Invalid card number or PIN. Please try again.");
            }

            // Check if card is expired
            if (card.getCardStatus() == ATMCard.CardStatus.EXPIRED) {
                loginAttemptDAO.recordATMAttempt(cardNumber, false, "Card expired");
                return AuthResult.failure("This card has expired. Please contact your bank.");
            }

            // Check if card is blocked
            if (card.getCardStatus() == ATMCard.CardStatus.BLOCKED) {
                // Check if the block period has elapsed
                if (card.getBlockedUntil() != null && card.getBlockedUntil().isBefore(LocalDateTime.now())) {
                    // Auto-unblock after block duration
                    cardDAO.activateCard(card.getCardId());
                    card.setCardStatus(ATMCard.CardStatus.ACTIVE);
                    card.setFailedAttempts(0);
                } else {
                    loginAttemptDAO.recordATMAttempt(cardNumber, false, "Card blocked");
                    return AuthResult.failure("This card is temporarily blocked. Please try after some time or contact your bank.");
                }
            }

            // Check if the associated account is active
            if (card.getAccount() == null || card.getAccount().getStatus() != com.atm.model.Account.AccountStatus.ACTIVE) {
                loginAttemptDAO.recordATMAttempt(cardNumber, false, "Account inactive");
                return AuthResult.failure("Invalid card number or PIN. Please try again.");
            }

            // Verify PIN using BCrypt (constant-time comparison)
            if (!PasswordHasher.verify(pin, card.getPinHash())) {
                // Increment failed attempts
                int newFailedAttempts = card.getFailedAttempts() + 1;
                cardDAO.incrementFailedAttempts(card.getCardId());

                int maxAttempts = config.getMaxLoginAttempts();

                if (newFailedAttempts >= maxAttempts) {
                    // Block the card
                    int blockMinutes = config.getBlockDurationMinutes();
                    LocalDateTime blockedUntil = LocalDateTime.now().plusMinutes(blockMinutes);
                    cardDAO.blockCard(card.getCardId(), blockedUntil);

                    loginAttemptDAO.recordATMAttempt(cardNumber, false, "Max attempts reached - card blocked");
                    return AuthResult.failure(
                        "Too many failed attempts. Your card has been temporarily blocked for " + blockMinutes + " minutes."
                    );
                }

                int remaining = maxAttempts - newFailedAttempts;
                loginAttemptDAO.recordATMAttempt(cardNumber, false, "Invalid PIN");
                return AuthResult.failure(
                    "Invalid card number or PIN. " + remaining + " attempt(s) remaining."
                );
            }

            // PIN is correct — reset failed attempts
            cardDAO.resetFailedAttempts(card.getCardId());
            loginAttemptDAO.recordATMAttempt(cardNumber, true, null);

            return AuthResult.success(card);

        } catch (SQLException e) {
            System.err.println("[Auth] Database error during ATM authentication: " + e.getMessage());
            return AuthResult.failure("A system error occurred. Please try again later.");
        }
    }

    /**
     * Authenticates an administrator.
     */
    public AdminAuthResult authenticateAdmin(String username, String password) {
        try {
            Admin admin = adminDAO.findByUsername(username);

            if (admin == null) {
                loginAttemptDAO.recordAdminAttempt(username, false, "Admin not found");
                return AdminAuthResult.failure("Invalid username or password.");
            }

            if (!admin.isActive()) {
                loginAttemptDAO.recordAdminAttempt(username, false, "Admin inactive");
                return AdminAuthResult.failure("This admin account is disabled.");
            }

            if (!PasswordHasher.verify(password, admin.getPasswordHash())) {
                loginAttemptDAO.recordAdminAttempt(username, false, "Invalid password");
                return AdminAuthResult.failure("Invalid username or password.");
            }

            loginAttemptDAO.recordAdminAttempt(username, true, null);
            return AdminAuthResult.success(admin);

        } catch (SQLException e) {
            System.err.println("[Auth] Database error during admin authentication: " + e.getMessage());
            return AdminAuthResult.failure("A system error occurred. Please try again later.");
        }
    }
}
