package com.csims.ui.panels;

import com.csims.model.Notification;
import com.csims.model.Project;
import com.csims.model.TaskItem;
import com.csims.model.User;
import com.csims.security.SessionContext;
import com.csims.service.*;
import com.csims.service.impl.*;
import com.csims.ui.common.BaseTableModel;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final ProjectService projectService = new ProjectServiceImpl();
    private final TaskService taskService = new TaskServiceImpl();
    private final WorkforceService workforceService = new WorkforceServiceImpl();
    private final InventoryService inventoryService = new InventoryServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final SafetyService safetyService = new SafetyServiceImpl();
    private final NotificationService notificationService = new NotificationServiceImpl();

    private final JLabel lblHeader = new JLabel("Operational Intelligence Dashboard");
    private final JLabel lblLastUpdated = new JLabel();
    private final JButton btnRefresh = new JButton("Refresh");

    private final JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 12, 12));
    private final MetricCard card1 = new MetricCard("Metric 1", new Color(0, 102, 204));
    private final MetricCard card2 = new MetricCard("Metric 2", new Color(46, 125, 50));
    private final MetricCard card3 = new MetricCard("Metric 3", new Color(216, 67, 21));
    private final MetricCard card4 = new MetricCard("Metric 4", new Color(123, 31, 162));
    private final MetricCard card5 = new MetricCard("Metric 5", new Color(198, 40, 40));
    private final MetricCard card6 = new MetricCard("Metric 6", new Color(0, 131, 143));

    // Summary tables
    private final JTable tblTasks = new JTable();
    private final TaskSummaryTableModel tasksModel = new TaskSummaryTableModel();
    private final JTable tblAlerts = new JTable();
    private final AlertSummaryTableModel alertsModel = new AlertSummaryTableModel();

    private static class MetricCard extends JPanel {
        private final JLabel lblTitle = new JLabel("Metric", SwingConstants.CENTER);
        private final JLabel lblValue = new JLabel("-", SwingConstants.CENTER);

        public MetricCard(String title, Color accentColor) {
            setLayout(new BorderLayout(6, 6));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            setBackground(Color.WHITE);

            lblTitle.setText(title);
            lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 12f));
            lblTitle.setForeground(new Color(60, 60, 60));

            lblValue.setFont(lblValue.getFont().deriveFont(Font.BOLD, 24f));
            lblValue.setForeground(accentColor);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
        }

        public void update(String title, String value) {
            lblTitle.setText(title);
            lblValue.setText(value);
        }
    }

    private static class TaskSummaryTableModel extends BaseTableModel<TaskItem> {
        public TaskSummaryTableModel() {
            super(new String[]{"ID", "Project", "Task", "Priority", "Status", "Due Date"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TaskItem t = getItemAt(rowIndex);
            if (t == null) return null;
            return switch (columnIndex) {
                case 0 -> t.getTaskId();
                case 1 -> t.getProjectName() != null ? t.getProjectName() : "Project #" + t.getProjectId();
                case 2 -> t.getTitle();
                case 3 -> t.getPriority();
                case 4 -> t.getStatus();
                case 5 -> DateUtil.formatDisplayDate(t.getDueDate());
                default -> "";
            };
        }
    }

    private static class AlertSummaryTableModel extends BaseTableModel<Notification> {
        public AlertSummaryTableModel() {
            super(new String[]{"ID", "Time", "Type", "Title", "Message"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Notification n = getItemAt(rowIndex);
            if (n == null) return null;
            return switch (columnIndex) {
                case 0 -> n.getNotificationId();
                case 1 -> DateUtil.formatDateTime(n.getCreatedAt());
                case 2 -> n.getType();
                case 3 -> n.getTitle();
                case 4 -> n.getMessage();
                default -> "";
            };
        }
    }

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        lblHeader.setFont(lblHeader.getFont().deriveFont(Font.BOLD, 15f));
        topBar.add(lblHeader, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        lblLastUpdated.setFont(lblLastUpdated.getFont().deriveFont(Font.PLAIN, 11f));
        rightControls.add(lblLastUpdated);
        rightControls.add(btnRefresh);
        topBar.add(rightControls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Content: Cards + Tables
        JPanel centerPanel = new JPanel(new BorderLayout(10, 12));

        // Metric Cards Grid
        cardsGrid.add(card1);
        cardsGrid.add(card2);
        cardsGrid.add(card3);
        cardsGrid.add(card4);
        cardsGrid.add(card5);
        cardsGrid.add(card6);
        cardsGrid.setPreferredSize(new Dimension(0, 160));
        centerPanel.add(cardsGrid, BorderLayout.NORTH);

        // Bottom Split / Tabbed Tables
        JTabbedPane tabbedPane = new JTabbedPane();

        tblTasks.setModel(tasksModel);
        tblTasks.setRowHeight(22);
        tblTasks.getTableHeader().setReorderingAllowed(false);
        tabbedPane.addTab("Active & Overdue Tasks", new JScrollPane(tblTasks));

        tblAlerts.setModel(alertsModel);
        tblAlerts.setRowHeight(22);
        tblAlerts.getTableHeader().setReorderingAllowed(false);
        tabbedPane.addTab("Recent System Alerts", new JScrollPane(tblAlerts));

        centerPanel.add(tabbedPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        User user = SessionContext.getInstance().getCurrentUser();
        String role = user != null && user.getRoleName() != null ? user.getRoleName() : "General";
        lblHeader.setText("Site Intelligence Dashboard (" + role + ")");
        lblLastUpdated.setText("Updated: " + DateUtil.formatDateTime(LocalDateTime.now()));

        try {
            // Load KPI metrics based on role
            loadKpiCards(role);

            // Load tables
            List<TaskItem> tasks = taskService.getAllTasks().stream()
                .filter(t -> !"Completed".equalsIgnoreCase(t.getStatus()))
                .limit(20)
                .toList();
            tasksModel.setItems(tasks);

            List<Notification> alerts = notificationService.getMyNotifications(false).stream()
                .limit(20)
                .toList();
            alertsModel.setItems(alerts);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load dashboard data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadKpiCards(String role) {
        try {
            switch (role) {
                case "Safety Officer" -> {
                    Map<String, Integer> vStats = safetyService.getViolationStats();
                    Map<String, Integer> iStats = safetyService.getIncidentStats();
                    double avgScore = safetyService.getAverageSafetyScore();

                    int openV = vStats.getOrDefault("OPEN", 0) + vStats.getOrDefault("IN_PROGRESS", 0);
                    int critV = vStats.getOrDefault("CRITICAL", 0);
                    int totalInc = iStats.values().stream().mapToInt(Integer::intValue).sum();
                    int totalInsp = safetyService.getAllInspections().size();

                    card1.update("Avg Safety Score", String.format("%.1f / 100", avgScore));
                    card2.update("Open Violations", String.valueOf(openV));
                    card3.update("Critical Violations", String.valueOf(critV));
                    card4.update("Total Inspections", String.valueOf(totalInsp));
                    card5.update("Total Incidents", String.valueOf(totalInc));
                    card6.update("System Alerts", String.valueOf(notificationService.getUnreadCount()));
                }
                case "Inventory Manager" -> {
                    var lowStockList = inventoryService.getLowStockMaterials();
                    var allMaterials = inventoryService.getAllMaterials();
                    double totalValue = allMaterials.stream()
                        .mapToDouble(m -> (m.getQuantityOnHand() != null ? m.getQuantityOnHand().doubleValue() : 0.0) *
                                          (m.getUnitPrice() != null ? m.getUnitPrice().doubleValue() : 0.0))
                        .sum();

                    card1.update("Material SKUs", String.valueOf(allMaterials.size()));
                    card2.update("Low Stock Items", String.valueOf(lowStockList.size()));
                    card3.update("Inventory Valuation", String.format("$%,.2f", totalValue));
                    card4.update("Stock OK", String.valueOf(allMaterials.size() - lowStockList.size()));
                    card5.update("Overdue Tasks", String.valueOf(taskService.getOverdueTasks().size()));
                    card6.update("Unread Alerts", String.valueOf(notificationService.getUnreadCount()));
                }
                case "Equipment Manager" -> {
                    Map<String, Integer> eqStats = equipmentService.getEquipmentStats();
                    int total = eqStats.values().stream().mapToInt(Integer::intValue).sum();
                    int avail = eqStats.getOrDefault("Available", 0);
                    int inUse = eqStats.getOrDefault("In Use", 0);
                    int maint = eqStats.getOrDefault("Under Maintenance", 0);

                    card1.update("Total Fleet Units", String.valueOf(total));
                    card2.update("Available Fleet", String.valueOf(avail));
                    card3.update("Units In Use", String.valueOf(inUse));
                    card4.update("Under Maintenance", String.valueOf(maint));
                    card5.update("Overdue Tasks", String.valueOf(taskService.getOverdueTasks().size()));
                    card6.update("Unread Alerts", String.valueOf(notificationService.getUnreadCount()));
                }
                case "Worker" -> {
                    User user = SessionContext.getInstance().getCurrentUser();
                    List<TaskItem> myTasks = taskService.getAllTasks().stream()
                        .filter(t -> t.getAssignedWorkerId() != null && t.getAssignedWorkerId().equals(user.getUserId()))
                        .toList();
                    long comp = myTasks.stream().filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();
                    long prog = myTasks.stream().filter(t -> "In Progress".equalsIgnoreCase(t.getStatus())).count();

                    card1.update("My Assigned Tasks", String.valueOf(myTasks.size()));
                    card2.update("In Progress", String.valueOf(prog));
                    card3.update("Completed", String.valueOf(comp));
                    card4.update("Pending", String.valueOf(myTasks.size() - comp - prog));
                    card5.update("Overdue", String.valueOf(taskService.getOverdueTasks().size()));
                    card6.update("My Unread Alerts", String.valueOf(notificationService.getUnreadCount()));
                }
                default -> { // Project Manager, Administrator, Site Engineer
                    List<Project> projects = projectService.getAllProjects();
                    long activeProj = projects.stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(p.getStatus())).count();
                    int totalWorkers = workforceService.getAllWorkers().size();
                    int overdue = taskService.getOverdueTasks().size();
                    int lowStock = inventoryService.getLowStockMaterials().size();
                    int unreadAlerts = notificationService.getUnreadCount();

                    card1.update("Total Projects", String.valueOf(projects.size()));
                    card2.update("Active Projects", String.valueOf(activeProj));
                    card3.update("Total Workforce", String.valueOf(totalWorkers));
                    card4.update("Overdue Tasks", String.valueOf(overdue));
                    card5.update("Low Stock Items", String.valueOf(lowStock));
                    card6.update("System Alerts", String.valueOf(unreadAlerts));
                }
            }
        } catch (Exception ex) {
            // Keep existing values on error
            card1.update("Error", "!");
        }
    }
}
