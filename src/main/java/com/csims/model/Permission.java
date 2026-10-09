package com.csims.model;

import java.io.Serializable;

/**
 * System permission in MODULE_ACTION format.
 */
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;

    private int permissionId;
    private String permissionCode;
    private String description;
    private String module;

    public Permission() {}

    public Permission(int permissionId, String permissionCode, String description, String module) {
        this.permissionId = permissionId;
        this.permissionCode = permissionCode;
        this.description = description;
        this.module = module;
    }

    public int getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public String toString() {
        return permissionCode;
    }
}
