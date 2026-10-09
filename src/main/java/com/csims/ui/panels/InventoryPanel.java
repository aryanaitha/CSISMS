package com.csims.ui.panels;

import com.csims.model.Material;
import com.csims.security.SessionContext;
import com.csims.service.InventoryService;
import com.csims.service.impl.InventoryServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.InventoryHistoryDialog;
import com.csims.ui.dialogs.MaterialFormDialog;
import com.csims.ui.dialogs.StockTransactionDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class InventoryPanel extends BasePanel {

    private final JCheckBox chkLowStockOnly = new JCheckBox("Low Stock Warnings Only", false);
    private final JButton btnRecordMovement = new JButton("Record Movement (Receipt/Issue)");
    private final JButton btnHistory = new JButton("Transaction History");

    private final MaterialTableModel tableModel = new MaterialTableModel();
    private final InventoryService inventoryService = new InventoryServiceImpl();

    private static class MaterialTableModel extends BaseTableModel<Material> {
        public MaterialTableModel() {
            super(new String[]{"ID", "Code", "Material Name", "Category", "Unit", "On Hand", "Min Limit", "Max Limit", "Price ($)", "Low Stock?"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Material m = getItemAt(rowIndex);
            if (m == null) return null;
            return switch (columnIndex) {
                case 0 -> m.getMaterialId();
                case 1 -> m.getMaterialCode();
                case 2 -> m.getName();
                case 3 -> m.getCategory();
                case 4 -> m.getUnit();
                case 5 -> m.getQuantityOnHand();
                case 6 -> m.getMinStock();
                case 7 -> m.getMaxStock();
                case 8 -> "$" + String.format("%.2f", m.getUnitPrice());
                case 9 -> m.isLowStock() ? "⚠️ LOW" : "OK";
                default -> "";
            };
        }
    }

    public InventoryPanel() {
        table.setModel(tableModel);

        topFilterPanel.add(chkLowStockOnly);
        bottomButtonPanel.add(btnRecordMovement);
        bottomButtonPanel.add(btnHistory);

        chkLowStockOnly.addActionListener(e -> onSearch());
        btnRecordMovement.addActionListener(this::onRecordMovement);
        btnHistory.addActionListener(e -> {
            InventoryHistoryDialog dlg = new InventoryHistoryDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
        });

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("INVENTORY_CREATE"));
        btnEdit.setEnabled(session.hasPermission("INVENTORY_UPDATE"));
        btnDelete.setEnabled(session.hasPermission("INVENTORY_UPDATE"));
        btnRecordMovement.setEnabled(session.hasPermission("INVENTORY_RECEIPT") || session.hasPermission("INVENTORY_ISSUE"));

        loadData();
    }

    @Override
    public void loadData() {
        String kw = txtSearch.getText().trim();
        boolean lowOnly = chkLowStockOnly.isSelected();
        try {
            List<Material> list = inventoryService.searchMaterials(kw.isEmpty() ? null : kw, null, lowOnly);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading materials: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        chkLowStockOnly.setSelected(false);
        loadData();
    }

    @Override
    protected void onAdd() {
        MaterialFormDialog dlg = new MaterialFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Material m = tableModel.getItemAt(idx);
        MaterialFormDialog dlg = new MaterialFormDialog(SwingUtilities.getWindowAncestor(this), m);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onDelete() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Material m = tableModel.getItemAt(idx);

        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete material '" + m.getName() + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                inventoryService.deleteMaterial(m.getMaterialId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onRecordMovement(ActionEvent e) {
        int idx = table.getSelectedRow();
        Material preSelected = null;
        if (idx != -1) {
            preSelected = tableModel.getItemAt(table.convertRowIndexToModel(idx));
        }

        StockTransactionDialog dlg = new StockTransactionDialog(SwingUtilities.getWindowAncestor(this), preSelected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            JOptionPane.showMessageDialog(this, "Material movement processed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }
}
