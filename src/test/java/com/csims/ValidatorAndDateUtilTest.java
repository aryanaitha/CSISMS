package com.csims;

import com.csims.util.DateUtil;
import com.csims.util.ValidationException;
import com.csims.util.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation & Date Utility Tests")
public class ValidatorAndDateUtilTest {

    @Test
    @DisplayName("Email format validation")
    void testEmailValidation() {
        assertDoesNotThrow(() -> Validator.validateEmail("john.doe@example.com", "Email"));
        assertDoesNotThrow(() -> Validator.validateEmail("admin123@construction.org", "Email"));

        assertThrows(ValidationException.class, () -> Validator.validateEmail("invalid-email", "Email"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("@nodomain.com", "Email"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("", "Email"));
    }

    @Test
    @DisplayName("Positive and range number validations")
    void testNumberValidation() {
        assertDoesNotThrow(() -> Validator.validatePositive(10.50, "Budget"));
        assertDoesNotThrow(() -> Validator.validatePositive(100.0, "Quantity"));
        assertDoesNotThrow(() -> Validator.validateRange(5, 1, 10, "Rating"));

        assertThrows(ValidationException.class, () -> Validator.validatePositive(-1.00, "Budget"));
        assertThrows(ValidationException.class, () -> Validator.validatePositive(0.0, "Quantity"));
        assertThrows(ValidationException.class, () -> Validator.validateRange(11, 1, 10, "Rating"));
    }

    @Test
    @DisplayName("Date formatting and parsing round-trip")
    void testDateUtil() {
        LocalDate date = LocalDate.of(2026, 10, 8);
        String formatted = DateUtil.formatDate(date);
        assertEquals("2026-10-08", formatted);

        String displayFormatted = DateUtil.formatDisplayDate(date);
        assertEquals("Oct 08, 2026", displayFormatted);

        LocalDate parsed = DateUtil.parseDate("2026-10-08");
        assertEquals(date, parsed);

        LocalDateTime now = LocalDateTime.of(2026, 10, 8, 14, 30, 0);
        String dtStr = DateUtil.formatDateTime(now);
        assertEquals("2026-10-08 14:30:00", dtStr);
    }
}
