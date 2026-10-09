package com.csims.ui;

import com.csims.model.User;
import com.csims.security.AccountLockedException;
import com.csims.security.AuthenticationException;
import com.csims.service.AuthService;
import com.csims.service.impl.AuthServiceImpl;
import com.csims.ui.dialogs.ChangePasswordDialog;
import com.csims.ui.dialogs.ForgotPasswordDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.prefs.Preferences;

public class LoginFrame extends JFrame {

    private static final String PREF_REMEMBER_USER = "csims_remembered_username";

    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JCheckBox chkShowPassword = new JCheckBox("Show password");
    private final JCheckBox chkRememberUser = new JCheckBox("Remember username");
    private final JLabel lblError = new JLabel(" ");

    private final JButton btnLogin = new JButton("Login");
    private final JButton btnBack = new JButton("Back");
    private final JButton btnForgotPassword = new JButton("Forgot password?");

    private final AuthService authService = new AuthServiceImpl();
    private final Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);

    private final LandingFrame landingFrame;

    public LoginFrame(LandingFrame landingFrame) {
        super("CSIMS - User Authentication");
        this.landingFrame = landingFrame;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 360);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Title Header
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel("Construction Site Intelligence - Login", SwingConstants.CENTER);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 15f));
        mainPanel.add(lblTitle, gbc);

        // Subtitle
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblSub = new JLabel("Enter your assigned credentials below", SwingConstants.CENTER);
        lblSub.setForeground(Color.GRAY);
        mainPanel.add(lblSub, gbc);

        // Error message label
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        lblError.setForeground(new Color(180, 0, 0));
        lblError.setFont(lblError.getFont().deriveFont(Font.BOLD, 11f));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblError, gbc);
        gbc.gridwidth = 1;

        // Username / Email field
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lblUser = new JLabel("Username or Email:");
        lblUser.setFont(lblUser.getFont().deriveFont(Font.BOLD));
        mainPanel.add(lblUser, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        mainPanel.add(txtUsername, gbc);
        row++;

        // Password field
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(lblPass.getFont().deriveFont(Font.BOLD));
        mainPanel.add(lblPass, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        mainPanel.add(txtPassword, gbc);
        row++;

        // Show password checkbox
        gbc.gridx = 1; gbc.gridy = row++;
        mainPanel.add(chkShowPassword, gbc);

        // Remember username checkbox
        gbc.gridx = 1; gbc.gridy = row++;
        mainPanel.add(chkRememberUser, gbc);

        // Buttons row
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnLogin.setPreferredSize(new Dimension(100, 28));
        btnBack.setPreferredSize(new Dimension(80, 28));
        pnlButtons.add(btnLogin);
        pnlButtons.add(btnBack);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        mainPanel.add(pnlButtons, gbc);

        // Forgot password link-style button
        styleAsLinkButton(btnForgotPassword);
        JPanel pnlForgot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlForgot.add(btnForgotPassword);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        mainPanel.add(pnlForgot, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Listeners
        chkShowPassword.addActionListener(e -> {
            txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '•');
        });

        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterSubmit);
        txtPassword.addKeyListener(enterSubmit);

        btnLogin.addActionListener(e -> doLogin());
        btnBack.addActionListener(e -> {
            setVisible(false);
            landingFrame.setVisible(true);
        });

        btnForgotPassword.addActionListener(e -> {
            ForgotPasswordDialog dlg = new ForgotPasswordDialog(this);
            dlg.setVisible(true);
        });

        // Load remembered username if saved
        loadRememberedUsername();
    }

    private void styleAsLinkButton(JButton btn) {
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setForeground(new Color(0, 70, 180));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void loadRememberedUsername() {
        String savedUser = prefs.get(PREF_REMEMBER_USER, "");
        if (!savedUser.isEmpty()) {
            txtUsername.setText(savedUser);
            chkRememberUser.setSelected(true);
            SwingUtilities.invokeLater(txtPassword::requestFocusInWindow);
        } else {
            SwingUtilities.invokeLater(txtUsername::requestFocusInWindow);
        }
    }

    private void doLogin() {
        lblError.setText(" ");
        String username = txtUsername.getText().trim();
        char[] password = txtPassword.getPassword();

        if (username.isEmpty()) {
            lblError.setText("Please enter your username or email.");
            txtUsername.requestFocusInWindow();
            return;
        }

        if (password.length == 0) {
            lblError.setText("Please enter your password.");
            txtPassword.requestFocusInWindow();
            return;
        }

        // Disable controls while authenticating
        setFormEnabled(false);
        btnLogin.setText("Authenticating...");

        // Offload to SwingWorker
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected User doInBackground() {
                try {
                    return authService.login(username, password);
                } catch (AccountLockedException ex) {
                    errorMessage = ex.getMessage();
                } catch (AuthenticationException ex) {
                    errorMessage = ex.getMessage();
                } catch (Exception ex) {
                    errorMessage = "Database connection error. Check db.properties.";
                }
                return null;
            }

            @Override
            protected void done() {
                Arrays.fill(password, '\0');
                txtPassword.setText("");
                setFormEnabled(true);
                btnLogin.setText("Login");

                try {
                    User user = get();
                    if (user != null) {
                        onLoginSuccess(user);
                    } else {
                        lblError.setText(errorMessage != null ? errorMessage : "Invalid credentials.");
                        txtPassword.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    lblError.setText("Unexpected login failure: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void onLoginSuccess(User user) {
        // Handle Remember Username
        if (chkRememberUser.isSelected()) {
            prefs.put(PREF_REMEMBER_USER, user.getUsername());
        } else {
            prefs.remove(PREF_REMEMBER_USER);
        }

        // Check if forced password change is required
        if (user.isMustChangePassword()) {
            ChangePasswordDialog dlg = new ChangePasswordDialog(this, user.getUserId(), true);
            dlg.setVisible(true);
            if (!dlg.isSuccess()) {
                // User aborted forced password change
                authService.logout();
                lblError.setText("Password change was not completed. Login aborted.");
                return;
            }
        }

        // Open MainFrame
        MainFrame mainFrame = new MainFrame(landingFrame);
        mainFrame.setVisible(true);
        setVisible(false);
    }

    private void setFormEnabled(boolean enabled) {
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        chkShowPassword.setEnabled(enabled);
        chkRememberUser.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
        btnBack.setEnabled(enabled);
        btnForgotPassword.setEnabled(enabled);
    }
}
