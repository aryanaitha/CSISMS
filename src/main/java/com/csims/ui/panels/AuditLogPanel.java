package com.csims.ui.panels;

import com.csims.model.AuditLog;
import com.csims.service.AuditService;
import com.csims.service.impl.AuditServiceImpl;
import com.csims.ui.common.BaseTableModel;
import com.csims.util.CsvExporter;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AuditLogPanel extends JPanel {

    private final JTextField txtUsername = new JTextField(10);
    private final JComboBox<String> cmbModule = new JComboBox<>(new String[]{
        "ALL", "AUTH", "USER", "PROJECT", "WORKFORCE", "TASK", "ATTENDANCE", "INVENTORY", "EQUIPMENT", "MAINTENANCE", "SAFETY"
    });
    private final JComboBox<String> cmbAction = new JComboBox<>(new String[]{
        "ALL", "LOGIN_SUCCESS", "LOGIN_FAILED", "CREATE", "UPDATE", "DELETE", "PASSWORD_CHANGE", "LOCKOUT", "STATUS_CHANGE"
    });
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ALL", "SUCCESS", "FAILURE"});

    private final JButton btnSearch = new JButton("Filter");
    private final JButton btnReset = new JButton("Reset");
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnViewDetails = new JButton("View Diffs / Details");
    private final JButton btnExportCsv = new JButton("Export CSV");

    private final JTable table = new JTable();
    private final AuditTableModel tableModel = new AuditTableModel();
    private final AuditService auditService = new AuditServiceImpl();

    private static class AuditTableModel extends BaseTableModel<AuditLog> {
        public AuditTableModel() {
            super(new String[]{"ID", "Timestamp", "User", "Role", "Module", "Action", "Entity ID", "Status", "Description"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AuditLog a = getItemAt(rowIndex);
            if (a == null) return null;
            return switch (columnIndex) {
                case 0 -> a.getAuditId();
                case 1 -> DateUtil.formatDateTime(a.getTimestamp());
                case 2 -> a.getUsername() != null ? a.getUsername() : "User #" + a.getUserId();
                case 3 -> a.getRole() != null ? a.getRole() : "-";
                case 4 -> a.getModule();
                case 5 -> a.getAction();
                case 6 -> a.getEntityId() != null ? a.getEntityId() : "-";
                case 7 -> a.getStatus();
                case 8 -> a.getDescription() != null ? a.getDescription() : "";
                default -> "";
            };
        }
    }

    public AuditLogPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Filter Bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        topPanel.add(new JLabel("User:"));
        topPanel.add(txtUsername);
        topPanel.add(new JLabel("Module:"));
        topPanel.add(cmbModule);
        topPanel.add(new JLabel("Action:"));
        topPanel.add(cmbAction);
        topPanel.add(new JLabel("Status:"));
        topPanel.add(cmbStatus);
        topPanel.add(btnSearch);
        topPanel.add(btnReset);
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // Center Table (strictly read-only)
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.add(btnViewDetails);
        bottomPanel.add(btnExportCsv);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        btnSearch.addActionListener(e -> loadData());
        btnReset.addActionListener(e -> onReset());
        btnRefresh.addActionListener(e -> loadData());
        btnViewDetails.addActionListener(e -> onViewDetails());
        btnExportCsv.addActionListener(e -> onExportCsv());

        loadData();
    }

    public void loadData() {
        String user = txtUsername.getText().trim();
        String mod = (String) cmbModule.getSelectedItem();
        String act = (String) cmbAction.getSelectedItem();
        String st = (String) cmbStatus.getSelectedItem();

        try {
            List<AuditLog> list = auditService.searchAuditLogs(
                user.isEmpty() ? null : user,
                "ALL".equalsIgnoreCase(act) ? null : act,
                "ALL".equalsIgnoreCase(mod) ? null : mod,
                null,
                null,
                "ALL".equalsIgnoreCase(st) ? null : st
            );
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading audit trail: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReset() {
        txtUsername.setText("");
        cmbModule.setSelectedIndex(0);
        cmbAction.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        loadData();
    }

    private void onViewDetails() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an audit log entry first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        AuditLog log = tableModel.getItemAt(modelRow);
        if (log == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Audit Log Details - ID #" + log.getAuditId(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(550, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        content.add(new JLabel("Timestamp: " + DateUtil.formatDateTime(log.getTimestamp())));
        content.add(new JLabel("User: " + log.getUsername() + " (" + log.getRole() + ")"));
        content.add(new JLabel("Action: " + log.getAction() + " | Module: " + log.getModule()));
        content.add(new JLabel("Entity: " + log.getEntityType() + " #" + log.getEntityId() + " | Status: " + log.getStatus()));
        content.add(new JLabel("Description: " + (log.getDescription() != null ? log.getDescription() : "-")));
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        content.add(new JLabel("Old Value (Before):"));
        JTextArea txtOld = new JTextArea(4, 30);
        txtOld.setText(log.getOldValue() != null ? log.getOldValue() : "(None)");
        txtOld.setEditable(false);
        content.add(new JScrollPane(txtOld));
        content.add(Box.createRigidArea(new Dimension(0, 5)));

        content.add(new JLabel("New Value (After):"));
        JTextArea txtNew = new JTextArea(4, 30);
        txtNew.setText(log.getNewValue() != null ? log.getNewValue() : "(None)");
        txtNew.setEditable(false);
        content.add(new JScrollPane(txtNew));

        dlg.add(content, BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnClose);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void onExportCsv() {
        List<AuditLog> list = tableModel.getItems();
        if (list == null || list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No audit logs to export.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("audit_trail_export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getAbsolutePath() + ".csv");
            }
            try {
                String[] headers = new String[]{"ID", "Timestamp", "User", "Role", "Module", "Action", "Entity Type", "Entity ID", "Status", "Description", "Old Value", "New Value"};
                List<String[]> rows = new ArrayList<>();
                for (AuditLog a : list) {
                    rows.add(new String[]{
                        String.valueOf(a.getAuditId()),
                        DateUtil.formatDateTime(a.getTimestamp()),
                        a.getUsername(),
                        a.getRole(),
                        a.getModule(),
                        a.getAction(),
                        a.getEntityType(),
                        a.getEntityId(),
                        a.getStatus(),
                        a.getDescription(),
                        a.getOldValue(),
                        a.getNewValue()
                    });
                }
                CsvExporter.exportToFile(target, headers, rows);
                JOptionPane.showMessageDialog(this, "Audit logs exported to:\n" + target.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
