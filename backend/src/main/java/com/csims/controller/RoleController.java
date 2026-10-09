package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Permission;
import com.csims.model.Role;
import com.csims.security.CustomUserDetails;
import com.csims.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.ok(roleService.getAllRoles()));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.ok(roleService.getAllPermissions()));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Permission>>> getRolePermissions(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.getPermissionsByRoleId(id)));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateRolePermissions(@PathVariable Integer id,
                                                                   @RequestBody Map<String, List<Integer>> body,
                                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Integer> permissionIds = body.getOrDefault("permissionIds", List.of());
        roleService.updateRolePermissions(id, permissionIds, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Role permissions updated successfully", null));
    }
}
