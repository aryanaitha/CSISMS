package com.csims.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Consolidated Utilities for CSIMS.
 * Includes CSV export, string escaping, formatting, and constants.
 */
public final class Utils {

    private Utils() {}

    // ===== CSV EXPORT =====
    public static byte[] generateCsv(List<String> headers, List<List<String>> rows) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            if (headers != null && !headers.isEmpty()) {
                writer.println(toCsvRow(headers));
            }
            if (rows != null) {
                for (List<String> row : rows) {
                    writer.println(toCsvRow(row));
                }
            }
            writer.flush();
        }
        return baos.toByteArray();
    }

    private static String toCsvRow(List<String> columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escapeCsv(columns.get(i)));
        }
        return sb.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String s = value.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
