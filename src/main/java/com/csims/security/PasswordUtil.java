package com.csims.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;

/**
 * Utility for password hashing and verification using PBKDF2 with HMAC-SHA256.
 * Salt is generated per-user using SecureRandom and stored in Base64 encoding.
 * Verification uses constant-time byte comparison to protect against timing attacks.
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // 256-bit derived key
    private static final int SALT_BYTES = 16;   // 128-bit salt

    private PasswordUtil() {
        // Utility class
    }

    /**
     * Generates a cryptographically strong random salt.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a plaintext password with a Base64-encoded salt.
     */
    public static String hashPassword(char[] password, String saltBase64) {
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(saltBase64, "Salt cannot be null");
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error during password hashing: " + e.getMessage(), e);
        }
    }

    /**
     * Convenience method taking password as String.
     */
    public static String hashPassword(String password, String saltBase64) {
        if (password == null) {
            return null;
        }
        return hashPassword(password.toCharArray(), saltBase64);
    }

    /**
     * Verifies a password against the stored PBKDF2 hash using constant-time comparison.
     */
    public static boolean verifyPassword(char[] password, String saltBase64, String storedHashBase64) {
        if (password == null || saltBase64 == null || storedHashBase64 == null) {
            return false;
        }
        String computedHash = hashPassword(password, saltBase64);
        return slowEquals(computedHash.getBytes(), storedHashBase64.getBytes());
    }

    public static boolean verifyPassword(String password, String saltBase64, String storedHashBase64) {
        if (password == null) {
            return false;
        }
        return verifyPassword(password.toCharArray(), saltBase64, storedHashBase64);
    }

    /**
     * Constant-time comparison to mitigate timing attacks.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return false;
        }
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /**
     * Validates password against standard policy:
     * Minimum 8 characters, at least one letter, at least one digit.
     */
    public static boolean meetsPolicy(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }
}
