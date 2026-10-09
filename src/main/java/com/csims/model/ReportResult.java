package com.csims.model;

import java.io.Serializable;
import java.util.List;

public class ReportResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String title;
    private final String[] headers;
    private final List<String[]> rows;

    public ReportResult(String title, String[] headers, List<String[]> rows) {
        this.title = title;
        this.headers = headers;
        this.rows = rows;
    }

    public String getTitle() {
        return title;
    }

    public String[] getHeaders() {
        return headers;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows != null ? rows.size() : 0;
    }
}
