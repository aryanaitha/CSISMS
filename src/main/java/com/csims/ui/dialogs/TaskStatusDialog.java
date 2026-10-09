package com.csims.ui.dialogs;

import com.csims.model.TaskItem;
import com.csims.service.TaskService;
import com.csims.service.impl.TaskServiceImpl;
import com.csims.ui.common.FormDialog;

import javax.swing.*;
import java.awt.*;

public class TaskStatusDialog extends FormDialog {

    private final TaskItem task;
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Created", "Assigned", "In Progress", "Completed"});
    private final JSpinner spnProgress = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5));

    private final TaskService taskService = new TaskServiceImpl();

    public TaskStatusDialog(Window owner, TaskItem task) {
        super(owner, "Update Task Progress & Status", 420, 240);
        this.task = task;

        JTextField txtTitle = new JTextField(task.getTitle());
        txtTitle.setEditable(false);
        addFormField("Task:", txtTitle);

        JTextField txtCurrStatus = new JTextField(task.getStatus() + " (" + task.getCompletionPercent() + "%)");
        txtCurrStatus.setEditable(false);
        addFormField("Current Workflow:", txtCurrStatus);

        cmbStatus.setSelectedItem(task.getStatus());
        addFormField("New Status:", cmbStatus);

        spnProgress.setValue(task.getCompletionPercent());
        addFormField("Completion (%):", spnProgress);

        cmbStatus.addActionListener(e -> {
            if ("Completed".equals(cmbStatus.getSelectedItem())) {
                spnProgress.setValue(100);
            }
        });
    }

    @Override
    protected boolean validateForm() {
        return true;
    }

    @Override
    protected void applyChanges() {
        String targetStatus = (String) cmbStatus.getSelectedItem();
        int prog = (Integer) spnProgress.getValue();
        taskService.transitionStatus(task.getTaskId(), targetStatus, prog);
    }
}
