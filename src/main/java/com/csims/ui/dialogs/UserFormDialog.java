package com.csims.ui.dialogs;

import com.csims.model.Role;
import com.csims.model.User;
import com.csims.service.UserService;
import com.csims.service.impl.UserServiceImpl;
import com.csims.ui.common.FormDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserFormDialog extends FormDialog {

    private final JTextField txtUsername = new JTextField(18);
    private final JTextField txtFullName = new JTextField(18);
    private final JTextField txtEmail = new JTextField(18);
    private final JPasswordField txtTempPassword = new JPasswordField(18);
    private final JComboBox<RoleItem> cmbRole = new JComboBox<>();
    private final JCheckBox chkActive = new JCheckBox("Account is Active", true);

    private final User userToEdit;
    private final UserService userService = new UserServiceImpl();

    private static class RoleItem {
        final int id;
        final String name;

        RoleItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public UserFormDialog(Window owner, User userToEdit) {
        super(owner, userToEdit == null ? "Create New User" : "Edit User Account", 440, 360);
        this.userToEdit = userToEdit;

        loadRoles();

        addFormField("Full Name:", txtFullName);
        addFormField("Email Address:", txtEmail);
        addFormField("Username:", txtUsername);

        if (userToEdit == null) {
            addFormField("Temp Password:", txtTempPassword);
        } else {
            txtUsername.setEditable(false);
        }

        addFormField("Assigned Role:", cmbRole);
        addFormField("Status:", chkActive);

        if (userToEdit != null) {
            txtFullName.setText(userToEdit.getFullName());
            txtEmail.setText(userToEdit.getEmail());
            txtUsername.setText(userToEdit.getUsername());
            chkActive.setSelected(userToEdit.isActive());
            for (int i = 0; i < cmbRole.getItemCount(); i++) {
                if (cmbRole.getItemAt(i).id == userToEdit.getRoleId()) {
                    cmbRole.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void loadRoles() {
        try {
            List<Role> roles = userService.getAllRoles();
            for (Role r : roles) {
                cmbRole.addItem(new RoleItem(r.getRoleId(), r.getRoleName()));
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected boolean validateForm() {
        if (txtFullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtEmail.getText().trim().isEmpty() || !txtEmail.getText().contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (userToEdit == null && txtTempPassword.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Temporary password is required for new accounts.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        RoleItem selectedRole = (RoleItem) cmbRole.getSelectedItem();
        int roleId = selectedRole != null ? selectedRole.id : 8;

        if (userToEdit == null) {
            User newUser = new User();
            newUser.setFullName(txtFullName.getText().trim());
            newUser.setEmail(txtEmail.getText().trim());
            newUser.setUsername(txtUsername.getText().trim());
            newUser.setRoleId(roleId);
            newUser.setActive(chkActive.isSelected());
            userService.createUser(newUser, new String(txtTempPassword.getPassword()));
        } else {
            userToEdit.setFullName(txtFullName.getText().trim());
            userToEdit.setEmail(txtEmail.getText().trim());
            userToEdit.setRoleId(roleId);
            userToEdit.setActive(chkActive.isSelected());
            userService.updateUser(userToEdit);
        }
    }
}
