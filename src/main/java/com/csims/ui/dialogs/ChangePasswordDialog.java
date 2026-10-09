package com.csims.ui.dialogs;

import com.csims.security.PasswordUtil;
import com.csims.service.AuthService;
import com.csims.service.impl.AuthServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class ChangePasswordDialog extends JDialog {

    private final JPasswordField txtCurrentPassword = new JPasswordField(18);
    private final JPasswordField txtNewPassword = new JPasswordField(18);
    private final JPasswordField txtConfirmPassword = new JPasswordField(18);

    private final JButton btnSave = new JButton("Update Password");
    private final JButton btnCancel = new JButton("Cancel");

    private final int userId;
    private final boolean forced;
    private boolean success = false;

    private final AuthService authService = new AuthServiceImpl();

    public ChangePasswordDialog(Window owner, int userId, boolean forced) {
        super(owner, forced ? "Mandatory Password Change" : "Change Account Password", ModalityType.APPLICATION_MODAL);
        this.userId = userId;
        this.forced = forced;

        setDefaultCloseOperation(forced ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
        if (forced) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int ans = JOptionPane.showConfirmDialog(ChangePasswordDialog.this,
                        "Password update is required before accessing the system.\nCancel and return to login?",
                        "Confirm Exit", JOptionPane.YES_NO_OPTION);
                    if (ans == JOptionPane.YES_OPTION) {
                        dispose();
                    }
                }
            });
        }

        setSize(460, forced ? 280 : 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Notice
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblNotice = new JLabel(forced
            ? "<html><b>Password change required:</b> You must set a new password before continuing.<br/>Policy: min 8 characters, at least 1 letter and 1 digit.</html>"
            : "<html><b>Set New Password:</b> Must differ from current password.<br/>Policy: min 8 characters, at least 1 letter and 1 digit.</html>");
        formPanel.add(lblNotice, gbc);
        gbc.gridwidth = 1;

        // Current Password (only if not forced)
        if (!forced) {
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
            formPanel.add(new JLabel("Current Password:"), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            formPanel.add(txtCurrentPassword, gbc);
            row++;
        }

        // New Password
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        formPanel.add(txtNewPassword, gbc);
        row++;

        // Confirm New Password
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        formPanel.add(txtConfirmPassword, gbc);
        row++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> {
            if (forced) {
                int ans = JOptionPane.showConfirmDialog(this,
                    "Password update is required to proceed. Cancel and return to login?",
                    "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (ans == JOptionPane.YES_OPTION) {
                    dispose();
                }
            } else {
                dispose();
            }
        });
    }

    private void onSave() {
        char[] newPass = txtNewPassword.getPassword();
        char[] confirmPass = txtConfirmPassword.getPassword();

        if (newPass.length == 0) {
            JOptionPane.showMessageDialog(this, "New password cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Arrays.equals(newPass, confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and Confirm password do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String passStr = new String(newPass);
        if (!PasswordUtil.meetsPolicy(passStr)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long, contain at least one letter and at least one digit.", "Policy Violation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        char[] currPass = forced ? null : txtCurrentPassword.getPassword();

        try {
            boolean updated = authService.changePassword(userId, currPass, newPass, forced);
            if (updated) {
                success = true;
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Updating Password", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(newPass, '\0');
            Arrays.fill(confirmPass, '\0');
            if (currPass != null) Arrays.fill(currPass, '\0');
        }
    }

    public boolean isSuccess() {
        return success;
    }
}
