package com.csims.ui.dialogs;

import com.csims.model.Permission;
import com.csims.model.Role;
import com.csims.service.UserService;
import com.csims.service.impl.UserServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class RolePermissionsDialog extends JDialog {

    private final JComboBox<RoleItem> cmbRoles = new JComboBox<>();
    private final JPanel pnlPermissions = new JPanel();
    private final Map<Integer, JCheckBox> checkBoxesByPermId = new HashMap<>();

    private final JButton btnSave = new JButton("Save Permissions");
    private final JButton btnClose = new JButton("Close");

    private final UserService userService = new UserServiceImpl();

    private static class RoleItem {
        final int id;
        final String name;

        RoleItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public RolePermissionsDialog(Window owner) {
        super(owner, "Role & Permissions Matrix", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topBar.add(new JLabel("Select Role:"));
        topBar.add(cmbRoles);
        add(topBar, BorderLayout.NORTH);

        pnlPermissions.setLayout(new BoxLayout(pnlPermissions, BoxLayout.Y_AXIS));
        pnlPermissions.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(new JScrollPane(pnlPermissions), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.add(btnSave);
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);

        loadRoles();
        cmbRoles.addActionListener(e -> loadPermissionsForSelectedRole());
        btnSave.addActionListener(e -> savePermissions());
        btnClose.addActionListener(e -> dispose());

        if (cmbRoles.getItemCount() > 0) {
            cmbRoles.setSelectedIndex(0);
            loadPermissionsForSelectedRole();
        }
    }

    private void loadRoles() {
        try {
            List<Role> roles = userService.getAllRoles();
            for (Role r : roles) {
                cmbRoles.addItem(new RoleItem(r.getRoleId(), r.getRoleName()));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading roles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPermissionsForSelectedRole() {
        RoleItem selectedRole = (RoleItem) cmbRoles.getSelectedItem();
        if (selectedRole == null) return;

        pnlPermissions.removeAll();
        checkBoxesByPermId.clear();

        try {
            List<Permission> allPerms = userService.getAllPermissions();
            List<Permission> rolePerms = userService.getPermissionsForRole(selectedRole.id);
            Set<Integer> assignedIds = new HashSet<>();
            for (Permission p : rolePerms) {
                assignedIds.add(p.getPermissionId());
            }

            // Group by module
            Map<String, List<Permission>> grouped = new TreeMap<>();
            for (Permission p : allPerms) {
                grouped.computeIfAbsent(p.getModule(), k -> new ArrayList<>()).add(p);
            }

            for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
                JLabel lblModule = new JLabel("=== " + entry.getKey() + " ===");
                lblModule.setFont(lblModule.getFont().deriveFont(Font.BOLD));
                lblModule.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));
                pnlPermissions.add(lblModule);

                for (Permission p : entry.getValue()) {
                    JCheckBox chk = new JCheckBox(p.getPermissionCode() + " - " + p.getDescription());
                    chk.setSelected(assignedIds.contains(p.getPermissionId()));
                    if (selectedRole.id == 1) { // Administrator
                        chk.setEnabled(false); // Admin has all permissions by default
                        chk.setSelected(true);
                    }
                    checkBoxesByPermId.put(p.getPermissionId(), chk);
                    pnlPermissions.add(chk);
                }
            }
        } catch (Exception ex) {
            pnlPermissions.add(new JLabel("Error loading permissions: " + ex.getMessage()));
        }

        pnlPermissions.revalidate();
        pnlPermissions.repaint();
    }

    private void savePermissions() {
        RoleItem selectedRole = (RoleItem) cmbRoles.getSelectedItem();
        if (selectedRole == null) return;

        if (selectedRole.id == 1) {
            JOptionPane.showMessageDialog(this, "Administrator role inherently possesses all permissions.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Set<Integer> selectedPermIds = new HashSet<>();
        for (Map.Entry<Integer, JCheckBox> entry : checkBoxesByPermId.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedPermIds.add(entry.getKey());
            }
        }

        try {
            boolean success = userService.updateRolePermissions(selectedRole.id, selectedPermIds);
            if (success) {
                JOptionPane.showMessageDialog(this, "Permissions updated successfully for role " + selectedRole.name, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Saving Permissions", JOptionPane.ERROR_MESSAGE);
        }
    }
}
