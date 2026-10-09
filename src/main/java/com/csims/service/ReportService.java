package com.csims.service;

import com.csims.model.ReportResult;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ReportService {
    List<String> getAvailableReports();
    ReportResult generateReport(String reportName);
    void exportReportToCsv(ReportResult report, File targetFile) throws IOException;
}
