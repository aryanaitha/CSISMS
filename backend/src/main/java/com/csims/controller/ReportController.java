package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.dto.ReportResultDto;
import com.csims.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableReports() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAvailableReports()));
    }

    @GetMapping("/preview")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<ReportResultDto>> previewReport(@RequestParam String type) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generateReport(type)));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<byte[]> exportCsv(@RequestParam String type) throws IOException {
        byte[] csvData = reportService.exportReportToCsv(type);
        String filename = type.toLowerCase() + "_report.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
