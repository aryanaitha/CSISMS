package com.csims.model;

import java.io.Serializable;

/**
 * User role representing access privileges in the system.
 */
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    private int roleId;
    private String roleName;
    private String description;

    public Role() {}

    public Role(int roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return roleName;
    }
}
