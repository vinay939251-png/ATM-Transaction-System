package com.atm.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility for secure PIN and password hashing using BCrypt.
 * Never stores, logs, or returns plaintext PINs/passwords.
 */
public class PasswordHasher {

    // BCrypt cost factor — 12 is a good balance of security and speed
    private static final int COST_FACTOR = 12;

    private PasswordHasher() {
        // Utility class — no instantiation
    }

    /**
     * Hashes a plaintext PIN or password using BCrypt.
     * @param plaintext The PIN or password to hash (e.g., "1234")
     * @return The BCrypt hash string
     */
    public static String hash(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Cannot hash null or empty input.");
        }
        return BCrypt.withDefaults().hashToString(COST_FACTOR, plaintext.toCharArray());
    }

    /**
     * Verifies a plaintext PIN or password against a stored BCrypt hash.
     * Uses constant-time comparison to prevent timing attacks.
     * @param plaintext The plaintext to verify
     * @param hash The stored BCrypt hash
     * @return true if the plaintext matches the hash
     */
    public static boolean verify(String plaintext, String hash) {
        if (plaintext == null || hash == null) return false;
        BCrypt.Result result = BCrypt.verifyer().verify(plaintext.toCharArray(), hash);
        return result.verified;
    }
}
