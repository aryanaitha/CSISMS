package com.csims.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Consolidated Data Transfer Objects (DTOs) for CSIMS.
 * Preserves all request/response schemas and validation annotations.
 */
public class Dtos {

    // ===== GENERIC API RESPONSE =====
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String error;
        private String code;
        private LocalDateTime timestamp = LocalDateTime.now();

        public ApiResponse() {}

        public ApiResponse(boolean success, String message, T data, String error, String code) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.error = error;
            this.code = code;
        }

        public static <T> ApiResponse<T> ok(T data) {
            return new ApiResponse<>(true, null, data, null, null);
        }

        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data, null, null);
        }

        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, null, data, null, null);
        }

        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data, null, null);
        }

        public static <T> ApiResponse<T> error(String error) {
            return new ApiResponse<>(false, error, null, error, null);
        }

        public static <T> ApiResponse<T> error(String error, String code) {
            return new ApiResponse<>(false, error, null, error, code);
        }

        public static <T> ApiResponse<T> fail(String error, String code) {
            return new ApiResponse<>(false, error, null, error, code);
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // ===== AUTH DTOS =====
    public static class LoginRequest {
        @NotBlank(message = "Username or email is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequestDto {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Desired username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotNull(message = "Requested role is required")
        private Integer requestedRoleId;

        @NotBlank(message = "Reason is required")
        private String reason;

        public RegisterRequestDto() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public Integer getRequestedRoleId() { return requestedRoleId; }
        public void setRequestedRoleId(Integer requestedRoleId) { this.requestedRoleId = requestedRoleId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String oldPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        private String newPassword;

        public ChangePasswordRequest() {}

        public ChangePasswordRequest(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class ForgotPasswordRequest {
        @NotBlank(message = "Username or email is required")
        private String usernameOrEmail;

        public ForgotPasswordRequest() {}

        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
    }

    // ===== USER DTO =====
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private Integer roleId;
        private String roleName;
        private boolean active;
        private boolean mustChangePassword;
        private LocalDateTime lastLogin;
        private Set<String> permissions;

        public UserDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public Integer getRoleId() { return roleId; }
        public void setRoleId(Integer roleId) { this.roleId = roleId; }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public boolean isMustChangePassword() { return mustChangePassword; }
        public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    }

    // ===== REPORT DTO =====
    public static class ReportResultDto {
        private String reportTitle;
        private List<String> headers;
        private List<List<String>> rows;

        public ReportResultDto() {}

        public ReportResultDto(String reportTitle, List<String> headers, List<List<String>> rows) {
            this.reportTitle = reportTitle;
            this.headers = headers;
            this.rows = rows;
        }

        public String getReportTitle() { return reportTitle; }
        public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

        public List<String> getHeaders() { return headers; }
        public void setHeaders(List<String> headers) { this.headers = headers; }

        public List<List<String>> getRows() { return rows; }
        public void setRows(List<List<String>> rows) { this.rows = rows; }
    }
}
