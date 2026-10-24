package com.csims.service;

import com.csims.model.Permission;
import com.csims.model.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final AdminService adminService;

    public RoleService(AdminService adminService) {
        this.adminService = adminService;
    }

    public List<Role> getAllRoles() {
        return adminService.getAllRoles();
    }

    public Role getRoleById(Integer id) {
        return adminService.getRoleById(id);
    }

    public List<Permission> getAllPermissions() {
        return adminService.getAllPermissions();
    }

    public List<Permission> getPermissionsByRoleId(Integer roleId) {
        return adminService.getPermissionsByRoleId(roleId);
    }

    public void updateRolePermissions(Integer roleId, List<Integer> permissionIds, Long operatorId, String operatorUsername) {
        adminService.updateRolePermissions(roleId, permissionIds, operatorId, operatorUsername);
    }
}
