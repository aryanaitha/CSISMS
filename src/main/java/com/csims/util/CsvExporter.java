package com.csims.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility for exporting tabular datasets to RFC-4180 compliant CSV files.
 */
public final class CsvExporter {

    private CsvExporter() {}

    public static void exportToFile(File file, String[] headers, List<String[]> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // Write headers
            if (headers != null && headers.length > 0) {
                writer.write(toCsvRow(headers));
                writer.newLine();
            }

            // Write rows
            if (rows != null) {
                for (String[] row : rows) {
                    writer.write(toCsvRow(row));
                    writer.newLine();
                }
            }
        }
    }

    private static String toCsvRow(String[] columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escape(columns[i]));
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "\"\"";
        }
        String s = value.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
