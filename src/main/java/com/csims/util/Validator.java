package com.csims.util;

import java.util.regex.Pattern;

/**
 * Common input validation methods for service layer.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_.-]{3,30}$"
    );

    private Validator() {}

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty.");
        }
    }

    public static void validateEmail(String email, String fieldName) {
        requireNonEmpty(email, fieldName);
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException(fieldName, "Invalid email address format.");
        }
    }

    public static void validateUsername(String username, String fieldName) {
        requireNonEmpty(username, fieldName);
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new ValidationException(fieldName, "Username must be 3-30 characters (letters, numbers, underscores, dots, hyphens).");
        }
    }

    public static void validatePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be greater than zero.");
        }
    }

    public static void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName, fieldName + " cannot be negative.");
        }
    }

    public static void validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName, fieldName + " must be between " + min + " and " + max + ".");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
