package com.csims.ui.common;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reusable typed table model for CSIMS module tables.
 *
 * @param <T> entity type
 */
public abstract class BaseTableModel<T> extends AbstractTableModel {

    private final String[] columnNames;
    private final List<T> items = new ArrayList<>();

    public BaseTableModel(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public void setItems(List<T> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        fireTableDataChanged();
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    public T getItemAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            return items.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public abstract Object getValueAt(int rowIndex, int columnIndex);
}
