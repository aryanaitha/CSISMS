package com.csims.ui.dialogs;

import com.csims.model.Incident;
import com.csims.model.User;
import com.csims.security.SessionContext;
import com.csims.service.SafetyService;
import com.csims.service.UserService;
import com.csims.service.impl.SafetyServiceImpl;
import com.csims.service.impl.UserServiceImpl;
import com.csims.ui.common.FormDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class IncidentInvestigateDialog extends FormDialog {

    private final Incident incident;
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Reported", "Under Investigation", "Action Taken", "Resolved"});
    private final JComboBox<UserItem> cmbInvestigator = new JComboBox<>();
    private final JTextArea txtActionsTaken = new JTextArea(4, 20);

    private final SafetyService safetyService = new SafetyServiceImpl();
    private final UserService userService = new UserServiceImpl();

    private static class UserItem {
        final Integer id;
        final String name;

        UserItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public IncidentInvestigateDialog(Window owner, Incident incident) {
        super(owner, "Investigate & Resolve Incident", 480, 360);
        this.incident = incident;

        loadInvestigators();

        JTextField txtTitle = new JTextField(incident.getTitle() + " (" + incident.getSeverity() + ")");
        txtTitle.setEditable(false);
        addFormField("Incident:", txtTitle);

        JTextField txtCurrent = new JTextField(incident.getStatus());
        txtCurrent.setEditable(false);
        addFormField("Current Stage:", txtCurrent);

        cmbStatus.setSelectedItem(incident.getStatus());
        addFormField("Workflow Status:", cmbStatus);

        addFormField("Lead Investigator:", cmbInvestigator);

        txtActionsTaken.setText(incident.getActionsTaken() != null ? incident.getActionsTaken() : "");
        txtActionsTaken.setLineWrap(true);
        txtActionsTaken.setWrapStyleWord(true);
        addFormField("Actions Taken / Resolution:", new JScrollPane(txtActionsTaken));
    }

    private void loadInvestigators() {
        cmbInvestigator.addItem(new UserItem(null, "-- None Assigned --"));
        try {
            List<User> users = userService.getAllUsers();
            for (User u : users) {
                if (u.isActive()) {
                    cmbInvestigator.addItem(new UserItem(u.getUserId(), u.getFullName() + " (" + u.getRoleName() + ")"));
                }
            }
            if (incident.getInvestigatorId() != null) {
                for (int i = 0; i < cmbInvestigator.getItemCount(); i++) {
                    if (incident.getInvestigatorId().equals(cmbInvestigator.getItemAt(i).id)) {
                        cmbInvestigator.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                // Default to current user
                User curr = SessionContext.getInstance().getCurrentUser();
                if (curr != null) {
                    for (int i = 0; i < cmbInvestigator.getItemCount(); i++) {
                        if (curr.getUserId() == cmbInvestigator.getItemAt(i).id) {
                            cmbInvestigator.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected boolean validateForm() {
        return true;
    }

    @Override
    protected void applyChanges() {
        String newStatus = (String) cmbStatus.getSelectedItem();
        UserItem ui = (UserItem) cmbInvestigator.getSelectedItem();
        Integer invId = ui != null ? ui.id : null;
        String actions = txtActionsTaken.getText().trim();

        safetyService.transitionIncidentStatus(incident.getIncidentId(), newStatus, invId, actions);
    }
}
