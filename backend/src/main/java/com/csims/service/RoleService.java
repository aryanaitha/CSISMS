package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.Permission;
import com.csims.model.Role;
import com.csims.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    public RoleService(RoleRepository roleRepository, AuditService auditService) {
        this.roleRepository = roleRepository;
        this.auditService = auditService;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public List<Permission> getAllPermissions() {
        return roleRepository.findAllPermissions();
    }

    public List<Permission> getPermissionsByRoleId(Integer roleId) {
        return roleRepository.findPermissionsByRoleId(roleId);
    }

    @Transactional
    public void updateRolePermissions(Integer roleId, List<Integer> permissionIds, Long operatorId, String operatorUsername) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException("Role not found with ID " + roleId, HttpStatus.NOT_FOUND));

        if (roleId == 1) {
            throw new ApiException("Administrator permissions cannot be modified", HttpStatus.BAD_REQUEST);
        }

        roleRepository.updateRolePermissions(roleId, permissionIds);
        auditService.log(operatorId, operatorUsername, null, "ROLE_PERMISSIONS_UPDATE", "ROLE", "ROLE", String.valueOf(roleId), null, permissionIds.toString(), "SUCCESS", "Updated permissions for role " + role.getName());
    }
}
