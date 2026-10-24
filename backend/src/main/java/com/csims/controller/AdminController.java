package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Models.AccountRequest;
import com.csims.model.Models.Permission;
import com.csims.model.Models.Role;
import com.csims.model.Models.User;
import com.csims.security.CustomUserDetails;
import com.csims.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Consolidated Administration Controller.
 * Handles /api/users, /api/roles, and /api/account-requests.
 */
@RestController
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // =========================================================================
    // SECTION 1: USERS (/api/users)
    // =========================================================================

    @GetMapping("/api/users")
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @GetMapping("/api/users/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUserById(id)));
    }

    @PostMapping("/api/users")
    @PreAuthorize("hasAuthority('USER_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Long>> createUser(@RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        User u = new User();
        u.setUsername((String) body.get("username"));
        u.setEmail((String) body.get("email"));
        u.setFullName((String) body.get("fullName"));
        u.setRoleId(body.get("roleId") != null ? ((Number) body.get("roleId")).intValue() : 8);

        String rawPassword = (String) body.getOrDefault("password", "Welcome@123");
        Long id = adminService.createUser(u, rawPassword, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User created successfully", id));
    }

    @PutMapping("/api/users/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateUser(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        User u = new User();
        u.setEmail((String) body.get("email"));
        u.setFullName((String) body.get("fullName"));
        u.setRoleId(body.get("roleId") != null ? ((Number) body.get("roleId")).intValue() : 8);
        u.setActive(body.get("active") != null ? (Boolean) body.get("active") : true);

        adminService.updateUser(id, u, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", null));
    }

    @PatchMapping("/api/users/{id}/status")
    @PreAuthorize("hasAuthority('USER_STATUS') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, Boolean> body,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean active = body.getOrDefault("active", true);
        adminService.toggleUserStatus(id, active, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User status updated", null));
    }

    @PostMapping("/api/users/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_PASSWORD_RESET') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id,
                                                           @RequestBody(required = false) Map<String, String> body,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        String newPassword = body != null && body.containsKey("newPassword") ? body.get("newPassword") : "Reset@12345";
        adminService.resetPassword(id, newPassword, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully. User must change password on next login.", null));
    }

    // =========================================================================
    // SECTION 2: ROLES & PERMISSIONS (/api/roles)
    // =========================================================================

    @GetMapping("/api/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllRoles()));
    }

    @GetMapping("/api/roles/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllPermissions()));
    }

    @GetMapping("/api/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Permission>>> getRolePermissions(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getPermissionsByRoleId(id)));
    }

    @PutMapping("/api/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateRolePermissions(@PathVariable Integer id,
                                                                   @RequestBody Map<String, List<Integer>> body,
                                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Integer> permissionIds = body.getOrDefault("permissionIds", List.of());
        adminService.updateRolePermissions(id, permissionIds, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Role permissions updated successfully", null));
    }

    // =========================================================================
    // SECTION 3: ACCOUNT REQUESTS (/api/account-requests)
    // =========================================================================

    @GetMapping("/api/account-requests")
    @PreAuthorize("hasAuthority('ACCOUNT_REQUEST_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<AccountRequest>>> getAccountRequests(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAccountRequests(status)));
    }

    @PostMapping("/api/account-requests/{id}/approve")
    @PreAuthorize("hasAuthority('ACCOUNT_REQUEST_REVIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> approveRequest(@PathVariable Long id,
                                                            @RequestBody(required = false) Map<String, String> body,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String notes = body != null && body.containsKey("reviewNotes") ? body.get("reviewNotes") : "Approved by administrator";
        adminService.approveRequest(id, userDetails.getId(), userDetails.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.ok("Request approved successfully", null));
    }

    @PostMapping("/api/account-requests/{id}/reject")
    @PreAuthorize("hasAuthority('ACCOUNT_REQUEST_REVIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(@PathVariable Long id,
                                                           @RequestBody(required = false) Map<String, String> body,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        String notes = body != null && body.containsKey("reviewNotes") ? body.get("reviewNotes") : "Rejected by administrator";
        adminService.rejectRequest(id, userDetails.getId(), userDetails.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.ok("Request rejected", null));
    }
}
