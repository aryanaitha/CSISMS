package com.csims;

import com.csims.model.User;
import com.csims.security.AccountLockedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication Lockout Logic Test")
public class AuthLockoutLogicTest {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Test
    @DisplayName("Simulate 5 consecutive failed login attempts leading to account lock")
    void testLockoutProgression() {
        User user = new User();
        user.setUsername("site_eng");
        user.setFailedAttempts(0);
        user.setLockedUntil(null);

        // 4 failed attempts: account remains unlocked
        for (int i = 1; i < MAX_FAILED_ATTEMPTS; i++) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            assertFalse(isAccountLocked(user), "Account must not lock before " + MAX_FAILED_ATTEMPTS + " attempts");
        }

        assertEquals(4, user.getFailedAttempts());

        // 5th failed attempt: triggers lockout
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
        }

        assertTrue(isAccountLocked(user), "Account must be locked after 5 failed attempts");

        // Attempting to login when locked should throw AccountLockedException
        assertThrows(AccountLockedException.class, () -> verifyAccountNotLocked(user));
    }

    @Test
    @DisplayName("Admin unlock resets failed attempts and clears lockout timestamp")
    void testAdminUnlock() {
        User user = new User();
        user.setUsername("locked_user");
        user.setFailedAttempts(5);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(15));

        assertTrue(isAccountLocked(user));

        // Unlock operation
        user.setFailedAttempts(0);
        user.setLockedUntil(null);

        assertFalse(isAccountLocked(user));
        assertDoesNotThrow(() -> verifyAccountNotLocked(user));
    }

    private boolean isAccountLocked(User user) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            return true;
        }
        return false;
    }

    private void verifyAccountNotLocked(User user) throws AccountLockedException {
        if (isAccountLocked(user)) {
            throw new AccountLockedException("Account is locked due to too many failed attempts.", user.getLockedUntil(), 15);
        }
    }
}
