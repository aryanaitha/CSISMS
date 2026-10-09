package com.csims;

import com.csims.util.CsvExporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RFC-4180 CSV Exporter Tests")
public class CsvExporterTest {

    @Test
    @DisplayName("Export tabular data with quotes, commas, nulls and special characters correctly escaped")
    void testCsvExport(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("test_export.csv").toFile();

        String[] headers = new String[]{"ID", "Item Name", "Description", "Price"};
        List<String[]> rows = List.of(
            new String[]{"1", "Cement (Grade 53)", "50kg bag, ultra-high strength", "8.50"},
            new String[]{"2", "Steel \"Rebar\"", "Standard reinforcement", "45.00"},
            new String[]{"3", null, "No name specified", "0.00"}
        );

        CsvExporter.exportToFile(file, headers, rows);

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        List<String> lines = Files.readAllLines(file.toPath());
        assertEquals(4, lines.size(), "1 header line + 3 data rows");

        // Verify escaping
        assertEquals("\"ID\",\"Item Name\",\"Description\",\"Price\"", lines.get(0));
        assertTrue(lines.get(1).contains("\"50kg bag, ultra-high strength\""));
        assertTrue(lines.get(2).contains("\"Steel \"\"Rebar\"\"\""));
        assertTrue(lines.get(3).startsWith("\"3\",\"\""));
    }
}
