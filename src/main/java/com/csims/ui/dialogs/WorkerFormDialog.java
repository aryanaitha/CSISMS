package com.csims.ui.dialogs;

import com.csims.model.Contractor;
import com.csims.model.Worker;
import com.csims.service.WorkforceService;
import com.csims.service.impl.WorkforceServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class WorkerFormDialog extends FormDialog {

    private final JTextField txtFullName = new JTextField(18);
    private final JTextField txtTrade = new JTextField(18);
    private final JTextField txtEmail = new JTextField(18);
    private final JTextField txtPhone = new JTextField(18);
    private final JComboBox<ContractorItem> cmbContractor = new JComboBox<>();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "ON_LEAVE"});
    private final JTextField txtJoinedDate = new JTextField(12);

    private final Worker workerToEdit;
    private final WorkforceService workforceService = new WorkforceServiceImpl();

    private static class ContractorItem {
        final Integer id;
        final String name;

        ContractorItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public WorkerFormDialog(Window owner, Worker workerToEdit) {
        super(owner, workerToEdit == null ? "Add Worker" : "Edit Worker Profile", 440, 380);
        this.workerToEdit = workerToEdit;

        loadContractors();

        addFormField("Full Name:", txtFullName);
        addFormField("Trade / Specialization:", txtTrade);
        addFormField("Email Address:", txtEmail);
        addFormField("Phone Number:", txtPhone);
        addFormField("Subcontractor:", cmbContractor);
        addFormField("Status:", cmbStatus);
        addFormField("Joined Date (YYYY-MM-DD):", txtJoinedDate);

        if (workerToEdit != null) {
            txtFullName.setText(workerToEdit.getFullName());
            txtTrade.setText(workerToEdit.getTrade());
            txtEmail.setText(workerToEdit.getEmail());
            txtPhone.setText(workerToEdit.getPhone());
            cmbStatus.setSelectedItem(workerToEdit.getStatus());
            txtJoinedDate.setText(DateUtil.formatDate(workerToEdit.getJoinedDate()));

            if (workerToEdit.getContractorId() != null) {
                for (int i = 0; i < cmbContractor.getItemCount(); i++) {
                    if (workerToEdit.getContractorId().equals(cmbContractor.getItemAt(i).id)) {
                        cmbContractor.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else {
            txtJoinedDate.setText(DateUtil.formatDate(LocalDate.now()));
        }
    }

    private void loadContractors() {
        cmbContractor.addItem(new ContractorItem(null, "-- None (Direct Hire) --"));
        try {
            List<Contractor> contractors = workforceService.getAllContractors();
            for (Contractor c : contractors) {
                cmbContractor.addItem(new ContractorItem(c.getContractorId(), c.getCompanyName()));
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected boolean validateForm() {
        if (txtFullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Worker name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtTrade.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Trade is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        Worker w = workerToEdit != null ? workerToEdit : new Worker();
        w.setFullName(txtFullName.getText().trim());
        w.setTrade(txtTrade.getText().trim());
        w.setEmail(txtEmail.getText().trim());
        w.setPhone(txtPhone.getText().trim());

        ContractorItem selected = (ContractorItem) cmbContractor.getSelectedItem();
        w.setContractorId(selected != null ? selected.id : null);
        w.setStatus((String) cmbStatus.getSelectedItem());
        w.setJoinedDate(DateUtil.parseDate(txtJoinedDate.getText().trim()));

        if (workerToEdit == null) {
            workforceService.createWorker(w);
        } else {
            workforceService.updateWorker(w);
        }
    }
}
