package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.User;
import com.csims.security.CustomUserDetails;
import com.csims.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Long>> createUser(@RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        User u = new User();
        u.setUsername((String) body.get("username"));
        u.setEmail((String) body.get("email"));
        u.setFullName((String) body.get("fullName"));
        u.setRoleId(body.get("roleId") != null ? ((Number) body.get("roleId")).intValue() : 8);

        String rawPassword = (String) body.getOrDefault("password", "Welcome@123");
        Long id = userService.createUser(u, rawPassword, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User created successfully", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateUser(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        User u = new User();
        u.setEmail((String) body.get("email"));
        u.setFullName((String) body.get("fullName"));
        u.setRoleId(body.get("roleId") != null ? ((Number) body.get("roleId")).intValue() : 8);
        u.setActive(body.get("active") != null ? (Boolean) body.get("active") : true);

        userService.updateUser(id, u, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_STATUS') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, Boolean> body,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean active = body.getOrDefault("active", true);
        userService.toggleUserStatus(id, active, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User status updated", null));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_PASSWORD_RESET') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id,
                                                           @RequestBody(required = false) Map<String, String> body,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        String newPassword = body != null && body.containsKey("newPassword") ? body.get("newPassword") : "Reset@12345";
        userService.resetPassword(id, newPassword, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully. User must change password on next login.", null));
    }
}
