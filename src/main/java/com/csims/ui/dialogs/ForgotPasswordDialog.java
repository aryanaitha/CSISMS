package com.csims.ui.dialogs;

import com.csims.service.AuthService;
import com.csims.service.impl.AuthServiceImpl;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordDialog extends JDialog {

    private final JTextField txtIdentifier = new JTextField(20);
    private final JTextArea txtReason = new JTextArea(3, 20);
    private final JButton btnSubmit = new JButton("Submit Request");
    private final JButton btnCancel = new JButton("Cancel");

    private final AuthService authService = new AuthServiceImpl();

    public ForgotPasswordDialog(Window owner) {
        super(owner, "Forgot Password", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblInfo = new JLabel("<html>Enter your username or email address.<br/>An Administrator will verify and reset your credentials.</html>");
        formPanel.add(lblInfo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Username / Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(txtIdentifier, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtReason), gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnSubmit.addActionListener(e -> onSubmit());
        btnCancel.addActionListener(e -> dispose());
    }

    private void onSubmit() {
        String id = txtIdentifier.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username or email address.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authService.requestPasswordReset(id, txtReason.getText());
        } catch (Exception ignored) {
            // Suppress details to prevent account enumeration
        }

        // Always show the same generic confirmation message
        JOptionPane.showMessageDialog(this,
            "If an account exists for the provided details, a password reset request has been queued for administrator review.",
            "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
