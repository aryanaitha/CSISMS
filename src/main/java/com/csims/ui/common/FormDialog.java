package com.csims.ui.common;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable modal dialog for CRUD data entry forms.
 */
public abstract class FormDialog extends JDialog {

    protected final JPanel formPanel = new JPanel(new GridBagLayout());
    protected final JButton btnSave = new JButton("Save");
    protected final JButton btnCancel = new JButton("Cancel");
    protected boolean confirmed = false;

    private int gridY = 0;

    public FormDialog(Window owner, String title, int width, int height) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(width, height);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel contentWrapper = new JPanel(new BorderLayout(10, 10));
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentWrapper.add(new JScrollPane(formPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        contentWrapper.add(buttonPanel, BorderLayout.SOUTH);
        add(contentWrapper);
    }

    /**
     * Adds a labeled component row to the GridBagLayout form.
     */
    protected void addFormField(String labelText, JComponent field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        formPanel.add(lbl, gbc);

        // Component
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(field, gbc);

        gridY++;
    }

    protected abstract boolean validateForm();
    protected abstract void applyChanges();

    protected void onSave() {
        try {
            if (validateForm()) {
                applyChanges();
                confirmed = true;
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
