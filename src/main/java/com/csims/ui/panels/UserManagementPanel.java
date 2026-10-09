package com.csims.ui.panels;

import com.csims.model.Role;
import com.csims.model.User;
import com.csims.service.UserService;
import com.csims.service.impl.UserServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.AccountRequestsDialog;
import com.csims.ui.dialogs.RolePermissionsDialog;
import com.csims.ui.dialogs.UserFormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class UserManagementPanel extends BasePanel {

    private final JComboBox<RoleFilterItem> cmbRoleFilter = new JComboBox<>();
    private final JCheckBox chkActiveOnly = new JCheckBox("Active Only", false);

    private final JButton btnToggleStatus = new JButton("Activate/Deactivate");
    private final JButton btnResetPassword = new JButton("Reset Password");
    private final JButton btnUnlock = new JButton("Unlock Account");
    private final JButton btnPermissions = new JButton("Role Permissions");
    private final JButton btnAccountRequests = new JButton("Review Requests");

    private final UserTableModel tableModel = new UserTableModel();
    private final UserService userService = new UserServiceImpl();

    private static class RoleFilterItem {
        final Integer id;
        final String name;

        RoleFilterItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class UserTableModel extends BaseTableModel<User> {
        public UserTableModel() {
            super(new String[]{"ID", "Username", "Full Name", "Email", "Role", "Active", "Locked Until", "Last Login"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            User u = getItemAt(rowIndex);
            if (u == null) return null;
            return switch (columnIndex) {
                case 0 -> u.getUserId();
                case 1 -> u.getUsername();
                case 2 -> u.getFullName();
                case 3 -> u.getEmail();
                case 4 -> u.getRoleName() != null ? u.getRoleName() : "Role #" + u.getRoleId();
                case 5 -> u.isActive() ? "Active" : "Inactive";
                case 6 -> u.getLockedUntil() != null ? DateUtil.formatDisplayDateTime(u.getLockedUntil()) : "No";
                case 7 -> u.getLastLogin() != null ? DateUtil.formatDisplayDateTime(u.getLastLogin()) : "Never";
                default -> "";
            };
        }
    }

    public UserManagementPanel() {
        table.setModel(tableModel);

        // Add filter items to top toolbar
        topFilterPanel.add(new JLabel("Role:"));
        cmbRoleFilter.addItem(new RoleFilterItem(null, "All Roles"));
        try {
            for (Role r : userService.getAllRoles()) {
                cmbRoleFilter.addItem(new RoleFilterItem(r.getRoleId(), r.getRoleName()));
            }
        } catch (Exception ignored) {}
        topFilterPanel.add(cmbRoleFilter);
        topFilterPanel.add(chkActiveOnly);

        // Add action buttons to bottom toolbar
        bottomButtonPanel.add(btnToggleStatus);
        bottomButtonPanel.add(btnResetPassword);
        bottomButtonPanel.add(btnUnlock);
        bottomButtonPanel.add(btnPermissions);
        bottomButtonPanel.add(btnAccountRequests);

        cmbRoleFilter.addActionListener(e -> onSearch());
        chkActiveOnly.addActionListener(e -> onSearch());

        btnToggleStatus.addActionListener(this::onToggleStatus);
        btnResetPassword.addActionListener(this::onResetPassword);
        btnUnlock.addActionListener(this::onUnlockAccount);
        btnPermissions.addActionListener(e -> {
            RolePermissionsDialog dlg = new RolePermissionsDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
        });
        btnAccountRequests.addActionListener(e -> {
            AccountRequestsDialog dlg = new AccountRequestsDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            loadData();
        });

        // Delete button in BasePanel is re-purposed or kept for user removal
        btnDelete.setText("Delete User");

        loadData();
    }

    @Override
    public void loadData() {
        RoleFilterItem selectedRole = (RoleFilterItem) cmbRoleFilter.getSelectedItem();
        Integer roleId = (selectedRole != null) ? selectedRole.id : null;
        Boolean activeOnly = chkActiveOnly.isSelected() ? Boolean.TRUE : null;
        String kw = txtSearch.getText().trim();

        try {
            List<User> list = userService.searchUsers(kw.isEmpty() ? null : kw, roleId, activeOnly);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        cmbRoleFilter.setSelectedIndex(0);
        chkActiveOnly.setSelected(false);
        loadData();
    }

    @Override
    protected void onAdd() {
        UserFormDialog dlg = new UserFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        User user = tableModel.getItemAt(idx);
        UserFormDialog dlg = new UserFormDialog(SwingUtilities.getWindowAncestor(this), user);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onDelete() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        User user = tableModel.getItemAt(idx);

        if (user.getUserId() == 1) {
            JOptionPane.showMessageDialog(this, "The primary Administrator account cannot be deleted.", "Restricted", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete user '" + user.getUsername() + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                userService.setUserActive(user.getUserId(), false);
                JOptionPane.showMessageDialog(this, "User account deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onToggleStatus(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        User user = tableModel.getItemAt(idx);

        if (user.getUserId() == 1) {
            JOptionPane.showMessageDialog(this, "The primary Administrator account cannot be deactivated.", "Restricted", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean newStatus = !user.isActive();
        try {
            userService.setUserActive(user.getUserId(), newStatus);
            JOptionPane.showMessageDialog(this, "User status updated to " + (newStatus ? "Active" : "Inactive"), "Updated", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onResetPassword(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        User user = tableModel.getItemAt(idx);

        String tempPass = JOptionPane.showInputDialog(this,
            "Enter temporary password for '" + user.getUsername() + "' (min 8 chars, 1 letter, 1 digit):", "Reset1234");
        if (tempPass == null || tempPass.trim().isEmpty()) return;

        try {
            boolean reset = userService.adminResetPassword(user.getUserId(), tempPass.trim());
            if (reset) {
                JOptionPane.showMessageDialog(this,
                    "Password reset successfully. The user will be forced to choose a new password on their next login.",
                    "Password Reset", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUnlockAccount(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        User user = tableModel.getItemAt(idx);

        try {
            boolean unlocked = userService.unlockUserAccount(user.getUserId());
            if (unlocked) {
                JOptionPane.showMessageDialog(this, "Lockout reset for '" + user.getUsername() + "'.", "Account Unlocked", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
