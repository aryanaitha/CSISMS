package com.csims.util;

import java.io.IOException;
import java.util.List;

public final class CsvExportUtil {
    private CsvExportUtil() {}

    public static byte[] generateCsv(List<String> headers, List<List<String>> rows) throws IOException {
        return Utils.generateCsv(headers, rows);
    }
}
