package com.csims.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Utility for formatting and parsing standard application dates and timestamps.
 */
public final class DateUtil {

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private DateUtil() {}

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FMT);
    }

    public static String formatDisplayDate(LocalDate date) {
        return date == null ? "" : date.format(DISPLAY_DATE_FMT);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATETIME_FMT);
    }

    public static String formatDisplayDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DISPLAY_DATETIME_FMT);
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(text.trim(), DATE_FMT);
    }

    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(text.trim(), DATETIME_FMT);
    }

    public static Date toSqlDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    public static LocalDate toLocalDate(Date sqlDate) {
        return sqlDate == null ? null : sqlDate.toLocalDate();
    }

    public static Timestamp toSqlTimestamp(LocalDateTime dt) {
        return dt == null ? null : Timestamp.valueOf(dt);
    }

    public static LocalDateTime toLocalDateTime(Timestamp sqlTs) {
        return sqlTs == null ? null : sqlTs.toLocalDateTime();
    }
}
