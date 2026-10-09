package com.csims.ui.dialogs;

import com.csims.model.AccountRequest;
import com.csims.model.Role;
import com.csims.service.AuthService;
import com.csims.service.UserService;
import com.csims.service.impl.AuthServiceImpl;
import com.csims.service.impl.UserServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RegisterRequestDialog extends JDialog {

    private final JTextField txtFullName = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtUsername = new JTextField(20);
    private final JComboBox<RoleItem> cmbRole = new JComboBox<>();
    private final JTextArea txtReason = new JTextArea(4, 20);

    private final JButton btnSubmit = new JButton("Submit Request");
    private final JButton btnCancel = new JButton("Cancel");

    private final AuthService authService = new AuthServiceImpl();
    private final UserService userService = new UserServiceImpl();

    private static class RoleItem {
        final int roleId;
        final String roleName;

        RoleItem(int id, String name) {
            this.roleId = id;
            this.roleName = name;
        }

        @Override
        public String toString() {
            return roleName;
        }
    }

    public RegisterRequestDialog(Window owner) {
        super(owner, "Request System Access", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(460, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Header info
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblHeader = new JLabel("<html><b>Submit your details for Administrator review.</b><br/>Accounts cannot be self-activated.</html>");
        formPanel.add(lblHeader, gbc);
        gbc.gridwidth = 1;

        // Full Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(txtFullName, gbc);
        row++;

        // Email
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(txtEmail, gbc);
        row++;

        // Desired Username
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Desired Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(txtUsername, gbc);
        row++;

        // Role (exclude Admin)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Requested Role:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        loadRoles();
        formPanel.add(cmbRole, gbc);
        row++;

        // Reason
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Reason / Justification:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtReason), gbc);
        row++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnSubmit.addActionListener(e -> onSubmit());
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadRoles() {
        try {
            List<Role> roles = userService.getAllRoles();
            for (Role r : roles) {
                if (r.getRoleId() != 1) { // Exclude Administrator
                    cmbRole.addItem(new RoleItem(r.getRoleId(), r.getRoleName()));
                }
            }
        } catch (Exception e) {
            // Fallback default roles if DB is offline during UI layout
            cmbRole.addItem(new RoleItem(2, "Project Manager"));
            cmbRole.addItem(new RoleItem(3, "Site Engineer"));
            cmbRole.addItem(new RoleItem(4, "Safety Officer"));
            cmbRole.addItem(new RoleItem(5, "Inventory Manager"));
            cmbRole.addItem(new RoleItem(6, "Equipment Manager"));
            cmbRole.addItem(new RoleItem(7, "Contractor"));
            cmbRole.addItem(new RoleItem(8, "Worker"));
        }
    }

    private void onSubmit() {
        RoleItem selectedRole = (RoleItem) cmbRole.getSelectedItem();
        if (selectedRole == null) {
            JOptionPane.showMessageDialog(this, "Please select a role.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AccountRequest req = new AccountRequest();
        req.setFullName(txtFullName.getText());
        req.setEmail(txtEmail.getText());
        req.setUsername(txtUsername.getText());
        req.setRequestedRoleId(selectedRole.roleId);
        req.setReason(txtReason.getText());

        try {
            authService.requestRegistration(req);
            JOptionPane.showMessageDialog(this,
                "Your registration request has been submitted successfully.\nAn Administrator will review it shortly.",
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
