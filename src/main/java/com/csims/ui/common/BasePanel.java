package com.csims.ui.common;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable base panel implementing the standard layout:
 * - North: Search and filter toolbar
 * - Center: JTable enclosed in JScrollPane
 * - South: Action buttons toolbar
 */
public abstract class BasePanel extends JPanel {

    protected final JPanel topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    protected final JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
    protected final JTable table = new JTable();
    protected final JScrollPane scrollPane = new JScrollPane(table);

    protected final JTextField txtSearch = new JTextField(15);
    protected final JButton btnSearch = new JButton("Search");
    protected final JButton btnReset = new JButton("Reset");

    protected final JButton btnAdd = new JButton("Add New");
    protected final JButton btnEdit = new JButton("Edit");
    protected final JButton btnDelete = new JButton("Delete");
    protected final JButton btnRefresh = new JButton("Refresh");

    public BasePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Configure default search controls
        topFilterPanel.add(new JLabel("Search:"));
        topFilterPanel.add(txtSearch);
        topFilterPanel.add(btnSearch);
        topFilterPanel.add(btnReset);

        btnSearch.addActionListener(e -> onSearch());
        btnReset.addActionListener(e -> onResetFilter());
        txtSearch.addActionListener(e -> onSearch());

        // Configure table
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);

        // Configure bottom action bar
        bottomButtonPanel.add(btnAdd);
        bottomButtonPanel.add(btnEdit);
        bottomButtonPanel.add(btnDelete);
        bottomButtonPanel.add(btnRefresh);

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> onRefresh());

        add(topFilterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    protected abstract void loadData();
    protected abstract void onSearch();
    protected abstract void onResetFilter();
    protected abstract void onAdd();
    protected abstract void onEdit();
    protected abstract void onDelete();

    protected void onRefresh() {
        loadData();
    }

    protected int getSelectedRowIndex() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }
}
