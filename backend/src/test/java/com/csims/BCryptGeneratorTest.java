package com.csims;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGeneratorTest {

    @Test
    void generateHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[] passwords = {
            "Admin@123", "Pm@12345", "Eng@12345", "Safe@12345",
            "Inv@12345", "Equip@12345", "Cont@12345", "Worker@12345"
        };
        for (String p : passwords) {
            System.out.println("BCRYPT:" + p + " -> " + encoder.encode(p));
        }
    }
}
