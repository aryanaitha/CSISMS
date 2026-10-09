package com.csims.ui.panels;

import com.csims.model.Notification;
import com.csims.service.NotificationService;
import com.csims.service.impl.NotificationServiceImpl;
import com.csims.ui.common.BaseTableModel;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NotificationPanel extends JPanel {

    private final JCheckBox chkUnreadOnly = new JCheckBox("Unread Only", false);
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnMarkRead = new JButton("Mark Selected as Read");
    private final JButton btnMarkAllRead = new JButton("Mark All as Read");

    private final JTable table = new JTable();
    private final NotificationTableModel tableModel = new NotificationTableModel();
    private final NotificationService notificationService = new NotificationServiceImpl();

    private static class NotificationTableModel extends BaseTableModel<Notification> {
        public NotificationTableModel() {
            super(new String[]{"ID", "Date & Time", "Type", "Title", "Message", "Status"});
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
                case 5 -> n.isRead() ? "Read" : "UNREAD";
                default -> "";
            };
        }
    }

    public NotificationPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top toolbar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(chkUnreadOnly);
        topPanel.add(btnRefresh);
        add(topPanel, BorderLayout.NORTH);

        // Center table
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom toolbar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.add(btnMarkRead);
        bottomPanel.add(btnMarkAllRead);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        chkUnreadOnly.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> loadData());
        btnMarkRead.addActionListener(e -> onMarkRead());
        btnMarkAllRead.addActionListener(e -> onMarkAllRead());

        loadData();
    }

    public void loadData() {
        try {
            boolean unread = chkUnreadOnly.isSelected();
            List<Notification> list = notificationService.getMyNotifications(unread);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onMarkRead() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Notification n = tableModel.getItemAt(modelRow);
        if (n != null) {
            try {
                notificationService.markAsRead(n.getNotificationId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update notification: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onMarkAllRead() {
        try {
            notificationService.markAllAsRead();
            loadData();
            JOptionPane.showMessageDialog(this, "All notifications marked as read.", "Notifications", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to mark all as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
