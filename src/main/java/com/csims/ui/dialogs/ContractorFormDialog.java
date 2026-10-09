package com.csims.ui.dialogs;

import com.csims.model.Contractor;
import com.csims.service.WorkforceService;
import com.csims.service.impl.WorkforceServiceImpl;
import com.csims.ui.common.FormDialog;

import javax.swing.*;
import java.awt.*;

public class ContractorFormDialog extends FormDialog {

    private final JTextField txtCompanyName = new JTextField(18);
    private final JTextField txtContactPerson = new JTextField(18);
    private final JTextField txtEmail = new JTextField(18);
    private final JTextField txtPhone = new JTextField(18);
    private final JTextArea txtAddress = new JTextArea(3, 18);

    private final Contractor contractorToEdit;
    private final WorkforceService workforceService = new WorkforceServiceImpl();

    public ContractorFormDialog(Window owner, Contractor contractorToEdit) {
        super(owner, contractorToEdit == null ? "Add Subcontractor" : "Edit Subcontractor", 430, 340);
        this.contractorToEdit = contractorToEdit;

        addFormField("Company Name:", txtCompanyName);
        addFormField("Contact Person:", txtContactPerson);
        addFormField("Email:", txtEmail);
        addFormField("Phone:", txtPhone);

        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        addFormField("Office Address:", new JScrollPane(txtAddress));

        if (contractorToEdit != null) {
            txtCompanyName.setText(contractorToEdit.getCompanyName());
            txtContactPerson.setText(contractorToEdit.getContactPerson());
            txtEmail.setText(contractorToEdit.getEmail());
            txtPhone.setText(contractorToEdit.getPhone());
            txtAddress.setText(contractorToEdit.getAddress());
        }
    }

    @Override
    protected boolean validateForm() {
        if (txtCompanyName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Company name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        Contractor c = contractorToEdit != null ? contractorToEdit : new Contractor();
        c.setCompanyName(txtCompanyName.getText().trim());
        c.setContactPerson(txtContactPerson.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setPhone(txtPhone.getText().trim());
        c.setAddress(txtAddress.getText().trim());

        if (contractorToEdit == null) {
            workforceService.createContractor(c);
        } else {
            workforceService.updateContractor(c);
        }
    }
}
