package com.csims.ui.panels;

import com.csims.model.ReportResult;
import com.csims.service.ReportService;
import com.csims.service.impl.ReportServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ReportPanel extends JPanel {

    private final JComboBox<String> cmbReportType = new JComboBox<>();
    private final JButton btnGenerate = new JButton("Generate Report");
    private final JButton btnExportCsv = new JButton("Export to CSV");
    private final JLabel lblStatus = new JLabel("Select a report and click Generate Report.");

    private final JTable table = new JTable();
    private final ReportService reportService = new ReportServiceImpl();
    private ReportResult currentReport;

    public ReportPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Populate available reports
        List<String> reportTypes = reportService.getAvailableReports();
        for (String r : reportTypes) {
            cmbReportType.addItem(r);
        }

        // Top Toolbar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(new JLabel("Report Type:"));
        topPanel.add(cmbReportType);
        topPanel.add(btnGenerate);
        topPanel.add(btnExportCsv);
        btnExportCsv.setEnabled(false);

        add(topPanel, BorderLayout.NORTH);

        // Center Table
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Status
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        bottomPanel.add(lblStatus);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        btnGenerate.addActionListener(e -> onGenerate());
        btnExportCsv.addActionListener(e -> onExportCsv());
    }

    private void onGenerate() {
        String selected = (String) cmbReportType.getSelectedItem();
        if (selected == null) return;

        try {
            currentReport = reportService.generateReport(selected);
            String[] headers = currentReport.getHeaders();
            List<String[]> rows = currentReport.getRows();

            DefaultTableModel model = new DefaultTableModel(headers, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (String[] row : rows) {
                model.addRow(row);
            }

            table.setModel(model);
            btnExportCsv.setEnabled(rows.size() > 0);
            lblStatus.setText("Generated '" + currentReport.getTitle() + "' with " + rows.size() + " record(s).");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportCsv() {
        if (currentReport == null || currentReport.getRows().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available to export.", "Export Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String safeName = currentReport.getTitle().toLowerCase().replaceAll("[^a-z0-9]", "_") + ".csv";
        chooser.setSelectedFile(new File(safeName));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getAbsolutePath() + ".csv");
            }

            try {
                reportService.exportReportToCsv(currentReport, target);
                JOptionPane.showMessageDialog(this,
                    "Report exported successfully to:\n" + target.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
