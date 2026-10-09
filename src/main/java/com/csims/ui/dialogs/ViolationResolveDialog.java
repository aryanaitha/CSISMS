package com.csims.ui.dialogs;

import com.csims.model.SafetyViolation;
import com.csims.service.SafetyService;
import com.csims.service.impl.SafetyServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class ViolationResolveDialog extends FormDialog {

    private final SafetyViolation violation;
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"});
    private final JTextArea txtCorrectiveAction = new JTextArea(3, 18);
    private final JTextField txtResolutionDate = new JTextField(12);

    private final SafetyService safetyService = new SafetyServiceImpl();

    public ViolationResolveDialog(Window owner, SafetyViolation violation) {
        super(owner, "Resolve Safety Violation", 450, 320);
        this.violation = violation;

        JTextField txtDesc = new JTextField(violation.getDescription());
        txtDesc.setEditable(false);
        addFormField("Violation:", txtDesc);

        cmbStatus.setSelectedItem(violation.getStatus());
        addFormField("Status:", cmbStatus);

        txtCorrectiveAction.setText(violation.getCorrectiveAction() != null ? violation.getCorrectiveAction() : "");
        txtCorrectiveAction.setLineWrap(true);
        txtCorrectiveAction.setWrapStyleWord(true);
        addFormField("Action Taken:", new JScrollPane(txtCorrectiveAction));

        txtResolutionDate.setText(DateUtil.formatDate(LocalDate.now()));
        addFormField("Resolution Date:", txtResolutionDate);
    }

    @Override
    protected boolean validateForm() {
        return true;
    }

    @Override
    protected void applyChanges() {
        String status = (String) cmbStatus.getSelectedItem();
        String action = txtCorrectiveAction.getText().trim();
        LocalDate rDate = DateUtil.parseDate(txtResolutionDate.getText().trim());

        safetyService.resolveViolation(violation.getViolationId(), status, action, rDate);
    }
}
