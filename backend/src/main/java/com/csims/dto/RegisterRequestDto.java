package com.csims.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequestDto {
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
