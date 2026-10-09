package com.csims.ui;

import com.csims.model.User;
import com.csims.security.IdleTimeoutManager;
import com.csims.security.SessionContext;
import com.csims.service.AuditService;
import com.csims.service.AuthService;
import com.csims.service.impl.AuditServiceImpl;
import com.csims.service.impl.AuthServiceImpl;
import com.csims.ui.dialogs.ChangePasswordDialog;
import com.csims.ui.panels.PlaceholderPanel;
import com.csims.ui.panels.UserManagementPanel;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final LandingFrame landingFrame;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);
    private final JPanel sidebarPanel = new JPanel();

    private final AuthService authService = new AuthServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();

    // Map of card name to instantiated panel
    private final Map<String, JComponent> modulePanels = new LinkedHashMap<>();

    public MainFrame(LandingFrame landingFrame) {
        super("CSIMS - Construction Site Intelligence & Safety Management System");
        this.landingFrame = landingFrame;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmAndLogout();
            }
        });

        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        User user = SessionContext.getInstance().getCurrentUser();
        String roleName = user != null && user.getRoleName() != null ? user.getRoleName() : "User";
        String fullName = user != null ? user.getFullName() : "Unknown";

        // 1. Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        JLabel lblUserInfo = new JLabel("Logged in as " + fullName + " (" + roleName + ")");
        lblUserInfo.setFont(lblUserInfo.getFont().deriveFont(Font.BOLD, 12f));
        topBar.add(lblUserInfo, BorderLayout.WEST);

        // Account Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAccount = new JMenu("Account Options");

        JMenuItem itemChangePass = new JMenuItem("Change Password");
        itemChangePass.addActionListener(e -> {
            if (user != null) {
                ChangePasswordDialog dlg = new ChangePasswordDialog(this, user.getUserId(), false);
                dlg.setVisible(true);
            }
        });

        JMenuItem itemLogout = new JMenuItem("Logout");
        itemLogout.addActionListener(e -> confirmAndLogout());

        menuAccount.add(itemChangePass);
        menuAccount.addSeparator();
        menuAccount.add(itemLogout);
        menuBar.add(menuAccount);
        topBar.add(menuBar, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // 2. Left Sidebar (text-only buttons)
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 8, 10, 8)
        ));
        sidebarPanel.setPreferredSize(new Dimension(190, 0));

        // Register module panels according to permissions
        registerModules();

        JScrollPane sidebarScroll = new JScrollPane(sidebarPanel);
        sidebarScroll.setBorder(null);
        sidebarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(sidebarScroll, BorderLayout.WEST);

        // 3. Center Cards Panel
        add(cardsPanel, BorderLayout.CENTER);

        // Start Idle Timeout Monitor (15 mins)
        IdleTimeoutManager.getInstance().start(() -> onIdleTimeout());
    }

    private void registerModules() {
        SessionContext session = SessionContext.getInstance();

        // 1. Dashboard (All users)
        addModule("Dashboard", null, new com.csims.ui.panels.DashboardPanel());

        // 2. User & Role Management (Admin only)
        if (session.hasPermission("USER_VIEW") || session.getCurrentUser().getRoleId() == 1) {
            addModule("User Management", "USER_VIEW", new UserManagementPanel());
        }

        // 3. Projects
        if (session.hasPermission("PROJECT_VIEW")) {
            addModule("Projects", "PROJECT_VIEW", new com.csims.ui.panels.ProjectPanel());
        }

        // 4. Workforce
        if (session.hasPermission("WORKFORCE_VIEW")) {
            addModule("Workforce", "WORKFORCE_VIEW", new com.csims.ui.panels.WorkforcePanel());
        }

        // 5. Tasks
        if (session.hasPermission("TASK_VIEW")) {
            addModule("Tasks", "TASK_VIEW", new com.csims.ui.panels.TaskPanel());
        }

        // 6. Attendance
        if (session.hasPermission("ATTENDANCE_VIEW")) {
            addModule("Attendance", "ATTENDANCE_VIEW", new com.csims.ui.panels.AttendancePanel());
        }

        // 7. Inventory
        if (session.hasPermission("INVENTORY_VIEW")) {
            addModule("Inventory", "INVENTORY_VIEW", new com.csims.ui.panels.InventoryPanel());
        }

        // 8. Equipment
        if (session.hasPermission("EQUIPMENT_VIEW")) {
            addModule("Equipment", "EQUIPMENT_VIEW", new com.csims.ui.panels.EquipmentPanel());
        }

        // 9. Maintenance
        if (session.hasPermission("MAINTENANCE_VIEW")) {
            addModule("Maintenance", "MAINTENANCE_VIEW", new com.csims.ui.panels.MaintenancePanel());
        }

        // 10. Safety Inspections
        if (session.hasPermission("SAFETY_INSPECTION_VIEW")) {
            addModule("Safety Inspections", "SAFETY_INSPECTION_VIEW", new com.csims.ui.panels.SafetyInspectionPanel());
        }

        // 11. Safety Violations
        if (session.hasPermission("SAFETY_VIOLATION_VIEW")) {
            addModule("Safety Violations", "SAFETY_VIOLATION_VIEW", new com.csims.ui.panels.SafetyViolationPanel());
        }

        // 12. Incidents
        if (session.hasPermission("INCIDENT_VIEW")) {
            addModule("Incidents", "INCIDENT_VIEW", new com.csims.ui.panels.IncidentPanel());
        }

        // 13. Notifications
        addModule("Notifications", null, new com.csims.ui.panels.NotificationPanel());

        // 14. Reports & CSV Export
        if (session.hasPermission("REPORT_VIEW")) {
            addModule("Reports", "REPORT_VIEW", new com.csims.ui.panels.ReportPanel());
        }

        // 15. Audit Logs (Admin only)
        if (session.hasPermission("AUDIT_VIEW") || session.getCurrentUser().getRoleId() == 1) {
            addModule("Audit Logs", "AUDIT_VIEW", new com.csims.ui.panels.AuditLogPanel());
        }

        sidebarPanel.add(Box.createVerticalGlue());
    }

    public void addModule(String title, String permissionCode, JComponent panel) {
        cardsPanel.add(panel, title);
        modulePanels.put(title, panel);

        JButton btn = new JButton(title);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(174, 32));
        btn.setPreferredSize(new Dimension(174, 32));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> showModule(title));

        sidebarPanel.add(btn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    public void setModulePanel(String title, JComponent panel) {
        cardsPanel.add(panel, title);
        modulePanels.put(title, panel);
    }

    public void showModule(String title) {
        cardLayout.show(cardsPanel, title);
    }

    private void confirmAndLogout() {
        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out of the system?",
            "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            performLogout();
        }
    }

    private void performLogout() {
        IdleTimeoutManager.getInstance().stop();
        authService.logout();
        dispose();
        landingFrame.setVisible(true);
    }

    private void onIdleTimeout() {
        User user = SessionContext.getInstance().getCurrentUser();
        auditService.log(user, "SESSION_TIMEOUT", "AUTH", "SESSION", null, null, null, "EXPIRED", "Session timed out due to 15 minutes of inactivity.");
        JOptionPane.showMessageDialog(this,
            "Your session has expired due to 15 minutes of inactivity.\nYou have been logged out.",
            "Session Timeout", JOptionPane.WARNING_MESSAGE);
        performLogout();
    }
}
