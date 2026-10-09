package com.csims.ui.dialogs;

import com.csims.model.AccountRequest;
import com.csims.service.UserService;
import com.csims.service.impl.UserServiceImpl;
import com.csims.ui.common.BaseTableModel;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AccountRequestsDialog extends JDialog {

    private final JTable table = new JTable();
    private final RequestTableModel tableModel = new RequestTableModel();
    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "REJECTED"});
    private final JComboBox<String> cmbTypeFilter = new JComboBox<>(new String[]{"ALL", "REGISTRATION", "PASSWORD_RESET"});

    private final JButton btnApprove = new JButton("Approve");
    private final JButton btnReject = new JButton("Reject");
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnClose = new JButton("Close");

    private final UserService userService = new UserServiceImpl();

    private static class RequestTableModel extends BaseTableModel<AccountRequest> {
        public RequestTableModel() {
            super(new String[]{"ID", "Type", "Full Name", "Username", "Email", "Role", "Status", "Reason", "Reviewer", "Review Note", "Submitted"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AccountRequest r = getItemAt(rowIndex);
            if (r == null) return null;
            return switch (columnIndex) {
                case 0 -> r.getRequestId();
                case 1 -> r.getRequestType();
                case 2 -> r.getFullName();
                case 3 -> r.getUsername();
                case 4 -> r.getEmail();
                case 5 -> r.getRequestedRoleName() != null ? r.getRequestedRoleName() : "Role #" + r.getRequestedRoleId();
                case 6 -> r.getStatus();
                case 7 -> r.getReason();
                case 8 -> r.getReviewerUsername() != null ? r.getReviewerUsername() : "";
                case 9 -> r.getReviewNote() != null ? r.getReviewNote() : "";
                case 10 -> DateUtil.formatDisplayDateTime(r.getCreatedAt());
                default -> "";
            };
        }
    }

    public AccountRequestsDialog(Window owner) {
        super(owner, "Review Account Requests", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(860, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.add(new JLabel("Type:"));
        topBar.add(cmbTypeFilter);
        topBar.add(new JLabel("Status:"));
        topBar.add(cmbStatusFilter);
        topBar.add(btnRefresh);
        add(topBar, BorderLayout.NORTH);

        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomBar.add(btnApprove);
        bottomBar.add(btnReject);
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);

        cmbTypeFilter.addActionListener(e -> loadRequests());
        cmbStatusFilter.addActionListener(e -> loadRequests());
        btnRefresh.addActionListener(e -> loadRequests());
        btnApprove.addActionListener(e -> onApprove());
        btnReject.addActionListener(e -> onReject());
        btnClose.addActionListener(e -> dispose());

        // Default to PENDING
        cmbStatusFilter.setSelectedItem("PENDING");
        loadRequests();
    }

    private void loadRequests() {
        String type = (String) cmbTypeFilter.getSelectedItem();
        String status = (String) cmbStatusFilter.getSelectedItem();
        try {
            List<AccountRequest> list = userService.getAccountRequests(null, type, status);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading requests: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onApprove() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request to approve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AccountRequest req = tableModel.getItemAt(table.convertRowIndexToModel(row));
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only PENDING requests can be approved.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String prompt = "REGISTRATION".equalsIgnoreCase(req.getRequestType())
            ? "Enter temporary password for new user '" + req.getUsername() + "' (min 8 chars, 1 letter, 1 digit):"
            : "Enter temporary password for reset user '" + req.getUsername() + "' (min 8 chars, 1 letter, 1 digit):";

        String tempPass = JOptionPane.showInputDialog(this, prompt, "Temp1234");
        if (tempPass == null || tempPass.trim().isEmpty()) {
            return;
        }

        String note = JOptionPane.showInputDialog(this, "Review note / comment (optional):", "Approved by Admin");

        try {
            boolean ok = userService.approveRequest(req.getRequestId(), tempPass.trim(), note);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Request #" + req.getRequestId() + " approved successfully!", "Approved", JOptionPane.INFORMATION_MESSAGE);
                loadRequests();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Approving Request", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReject() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request to reject.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AccountRequest req = tableModel.getItemAt(table.convertRowIndexToModel(row));
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only PENDING requests can be rejected.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this, "Enter rejection reason / note (required):");
        if (reason == null || reason.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Rejection reason is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean ok = userService.rejectRequest(req.getRequestId(), reason.trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Request #" + req.getRequestId() + " rejected.", "Rejected", JOptionPane.INFORMATION_MESSAGE);
                loadRequests();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Rejecting Request", JOptionPane.ERROR_MESSAGE);
        }
    }
}
