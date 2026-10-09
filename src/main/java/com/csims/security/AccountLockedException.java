package com.csims.security;

import java.time.LocalDateTime;

/**
 * Thrown when an account has been temporarily locked due to excessive failed attempts.
 */
public class AccountLockedException extends AuthenticationException {

    private final LocalDateTime lockedUntil;
    private final long remainingMinutes;

    public AccountLockedException(String message, LocalDateTime lockedUntil, long remainingMinutes) {
        super(message);
        this.lockedUntil = lockedUntil;
        this.remainingMinutes = remainingMinutes;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public long getRemainingMinutes() {
        return remainingMinutes;
    }
}
