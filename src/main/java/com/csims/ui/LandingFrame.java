package com.csims.ui;

import com.csims.ui.dialogs.RegisterRequestDialog;
import com.csims.util.DBConnection;

import javax.swing.*;
import java.awt.*;

public class LandingFrame extends JFrame {

    private final JLabel lblDbStatus = new JLabel("Database: Checking...", SwingConstants.CENTER);
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnRequestAccess = new JButton("Request Access");
    private final JButton btnExit = new JButton("Exit");

    private LoginFrame loginFrame;

    public LandingFrame() {
        super("CSIMS - Construction Site Intelligence & Safety Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // Center content panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 20, 40));

        // 1. Title
        JLabel lblTitle = new JLabel("Construction Site Intelligence & Safety Management System");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 17f));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblTitle);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // 2. Tagline
        JLabel lblTagline = new JLabel("Manage projects, people, materials, equipment and safety in one place.");
        lblTagline.setFont(lblTagline.getFont().deriveFont(Font.PLAIN, 13f));
        lblTagline.setForeground(Color.DARK_GRAY);
        lblTagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblTagline);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. Database connection status banner
        lblDbStatus.setFont(lblDbStatus.getFont().deriveFont(Font.BOLD, 12f));
        lblDbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblDbStatus);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 4. Key Modules Section (simple 2-column text table or grid)
        JLabel lblModulesTitle = new JLabel("Key System Modules:");
        lblModulesTitle.setFont(lblModulesTitle.getFont().deriveFont(Font.BOLD, 13f));
        lblModulesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblModulesTitle);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel pnlModules = new JPanel(new GridLayout(5, 2, 20, 4));
        pnlModules.setMaximumSize(new Dimension(460, 130));
        pnlModules.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] modulesCol1 = {"• Projects Management", "• Workforce Allocation", "• Task Scheduling & Workflow", "• Daily Attendance Sheets", "• Warehouse Materials & Stock"};
        String[] modulesCol2 = {"• Equipment & Machinery", "• Maintenance Service Tracking", "• Safety Audits & Inspections", "• Hazard Violations & Incidents", "• Intelligence Analytics & Reports"};

        for (int i = 0; i < 5; i++) {
            pnlModules.add(new JLabel(modulesCol1[i]));
            pnlModules.add(new JLabel(modulesCol2[i]));
        }
        centerPanel.add(pnlModules);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // 5. Who uses it line
        JLabel lblWho = new JLabel("Who uses it: Administrator, Project Manager, Site Engineer, Safety Officer, Inventory Manager, Equipment Manager, Contractor, Worker.");
        lblWho.setFont(lblWho.getFont().deriveFont(Font.ITALIC, 11f));
        lblWho.setForeground(new Color(60, 60, 60));
        lblWho.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblWho);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // 6. Action buttons
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnLogin.setPreferredSize(new Dimension(110, 32));
        btnRequestAccess.setPreferredSize(new Dimension(140, 32));
        btnExit.setPreferredSize(new Dimension(90, 32));

        pnlButtons.add(btnLogin);
        pnlButtons.add(btnRequestAccess);
        pnlButtons.add(btnExit);
        pnlButtons.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(pnlButtons);

        add(centerPanel, BorderLayout.CENTER);

        // 7. Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JLabel lblFooter = new JLabel("CSIMS — Version 1.0.0 — Mini Project");
        lblFooter.setFont(lblFooter.getFont().deriveFont(Font.PLAIN, 11f));
        lblFooter.setForeground(Color.GRAY);
        footerPanel.add(lblFooter);
        add(footerPanel, BorderLayout.SOUTH);

        // Actions
        btnLogin.addActionListener(e -> {
            if (loginFrame == null) {
                loginFrame = new LoginFrame(this);
            }
            loginFrame.setVisible(true);
            setVisible(false);
        });

        btnRequestAccess.addActionListener(e -> {
            RegisterRequestDialog dlg = new RegisterRequestDialog(this);
            dlg.setVisible(true);
        });

        btnExit.addActionListener(e -> System.exit(0));

        // Startup database connectivity check
        checkDatabaseConnection();
    }

    private void checkDatabaseConnection() {
        // Run check on background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return DBConnection.getInstance().isConnectionAvailable();
            }

            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (connected) {
                        lblDbStatus.setText("Database: Connected (MySQL)");
                        lblDbStatus.setForeground(new Color(0, 130, 40));
                        btnLogin.setEnabled(true);
                    } else {
                        lblDbStatus.setText("Database: Not connected — Please check db.properties and ensure MySQL is running.");
                        lblDbStatus.setForeground(new Color(180, 0, 0));
                        btnLogin.setEnabled(false);
                    }
                } catch (Exception e) {
                    lblDbStatus.setText("Database: Error checking connection: " + e.getMessage());
                    lblDbStatus.setForeground(new Color(180, 0, 0));
                    btnLogin.setEnabled(false);
                }
            }
        };
        worker.execute();
    }
}
