package com.csims.ui.dialogs;

import com.csims.model.InventoryTransaction;
import com.csims.service.InventoryService;
import com.csims.service.impl.InventoryServiceImpl;
import com.csims.ui.common.BaseTableModel;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InventoryHistoryDialog extends JDialog {

    private final JTable table = new JTable();
    private final TransactionTableModel tableModel = new TransactionTableModel();
    private final JComboBox<String> cmbTypeFilter = new JComboBox<>(new String[]{"ALL", "RECEIPT", "ISSUE", "ADJUSTMENT"});
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnClose = new JButton("Close");

    private final InventoryService inventoryService = new InventoryServiceImpl();

    private static class TransactionTableModel extends BaseTableModel<InventoryTransaction> {
        public TransactionTableModel() {
            super(new String[]{"ID", "Date & Time", "Type", "Material", "Quantity", "Project", "Reference", "Performed By", "Notes"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            InventoryTransaction tx = getItemAt(rowIndex);
            if (tx == null) return null;
            return switch (columnIndex) {
                case 0 -> tx.getTransactionId();
                case 1 -> DateUtil.formatDisplayDateTime(tx.getTransactionDate());
                case 2 -> tx.getTransactionType();
                case 3 -> tx.getMaterialName() != null ? tx.getMaterialName() : "Material #" + tx.getMaterialId();
                case 4 -> tx.getQuantity() + " " + (tx.getUnit() != null ? tx.getUnit() : "");
                case 5 -> tx.getProjectName() != null ? tx.getProjectName() : "General";
                case 6 -> tx.getReferenceNo() != null ? tx.getReferenceNo() : "";
                case 7 -> tx.getPerformerName() != null ? tx.getPerformerName() : "User #" + tx.getPerformedBy();
                case 8 -> tx.getRemarks() != null ? tx.getRemarks() : "";
                default -> "";
            };
        }
    }

    public InventoryHistoryDialog(Window owner) {
        super(owner, "Inventory Movement History", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(880, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.add(new JLabel("Movement Type:"));
        topBar.add(cmbTypeFilter);
        topBar.add(btnRefresh);
        add(topBar, BorderLayout.NORTH);

        table.setModel(tableModel);
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);

        cmbTypeFilter.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> loadData());
        btnClose.addActionListener(e -> dispose());

        loadData();
    }

    private void loadData() {
        String type = (String) cmbTypeFilter.getSelectedItem();
        try {
            List<InventoryTransaction> list = inventoryService.searchTransactions(null, null, type, null, null);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading transaction history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
