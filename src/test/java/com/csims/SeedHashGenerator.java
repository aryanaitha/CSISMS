package com.csims;

import com.csims.security.PasswordUtil;

public class SeedHashGenerator {
    public static void main(String[] args) {
        String[] passwords = {
            "Admin@123",
            "Manager@123",
            "Engineer@123",
            "Safety@123",
            "Inventory@123",
            "Equipment@123",
            "Contractor@123",
            "Worker@123",
            "Temp@1234"
        };
        for (String p : passwords) {
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(p, salt);
            System.out.println(p + " | SALT: " + salt + " | HASH: " + hash);
        }
    }
}
