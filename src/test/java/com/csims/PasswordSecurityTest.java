package com.csims;

import com.csims.security.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Security & Hashing Tests")
public class PasswordSecurityTest {

    @Test
    @DisplayName("Password hashing produces unique salts and distinct hashes for same plaintext")
    void testHashingUniqueness() {
        String plain = "Admin@1234";
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotEquals(salt1, salt2, "Salts must be randomly generated and unique");

        String hash1 = PasswordUtil.hashPassword(plain, salt1);
        String hash2 = PasswordUtil.hashPassword(plain, salt2);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2, "Hashes with different salts must differ");
    }

    @Test
    @DisplayName("Password verification succeeds with correct password and fails with wrong")
    void testVerification() {
        String plain = "SiteEng@2026";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plain, salt);

        assertTrue(PasswordUtil.verifyPassword(plain, salt, hash), "Correct password must verify successfully");
        assertFalse(PasswordUtil.verifyPassword("WrongPassword1!", salt, hash), "Incorrect password must fail verification");
        assertFalse(PasswordUtil.verifyPassword(plain.toLowerCase(), salt, hash), "Case sensitive check must fail");
    }

    @Test
    @DisplayName("Password policy enforcement: min 8 chars, at least one letter, at least one digit")
    void testPasswordPolicy() {
        assertTrue(PasswordUtil.meetsPolicy("StrongP@ss1"));
        assertTrue(PasswordUtil.meetsPolicy("Admin@1234"));

        assertFalse(PasswordUtil.meetsPolicy("short1"), "Too short (< 8 chars)");
        assertFalse(PasswordUtil.meetsPolicy("alllettersnodigit"), "No digits");
        assertFalse(PasswordUtil.meetsPolicy("1234567890"), "No letters");
        assertFalse(PasswordUtil.meetsPolicy(null), "Null password");
        assertFalse(PasswordUtil.meetsPolicy(""), "Empty password");
    }
}
