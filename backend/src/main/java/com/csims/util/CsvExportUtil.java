package com.csims.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExportUtil {

    private CsvExportUtil() {}

    public static byte[] generateCsv(List<String> headers, List<List<String>> rows) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            // Write headers
            if (headers != null && !headers.isEmpty()) {
                writer.println(toCsvRow(headers));
            }
            // Write rows
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
            sb.append(escape(columns.get(i)));
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
