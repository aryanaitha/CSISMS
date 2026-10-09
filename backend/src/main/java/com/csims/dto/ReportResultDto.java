package com.csims.dto;

import java.util.List;

public class ReportResultDto {
    private String reportTitle;
    private List<String> headers;
    private List<List<String>> rows;

    public ReportResultDto() {}

    public ReportResultDto(String reportTitle, List<String> headers, List<List<String>> rows) {
        this.reportTitle = reportTitle;
        this.headers = headers;
        this.rows = rows;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }
}
