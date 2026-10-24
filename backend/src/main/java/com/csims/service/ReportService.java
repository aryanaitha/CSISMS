package com.csims.service;

import com.csims.dto.ReportResultDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ReportService {
    private final SystemService systemService;

    public ReportService(SystemService systemService) {
        this.systemService = systemService;
    }

    public List<String> getAvailableReports() {
        return systemService.getAvailableReports();
    }

    public ReportResultDto generateReport(String type) {
        com.csims.dto.Dtos.ReportResultDto res = systemService.generateReport(type);
        return new ReportResultDto(res.getReportTitle(), res.getHeaders(), res.getRows());
    }

    public byte[] exportReportToCsv(String type) throws IOException {
        return systemService.exportReportToCsv(type);
    }
}
