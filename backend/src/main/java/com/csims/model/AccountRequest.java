package com.csims.model;

import java.time.LocalDateTime;

public class AccountRequest {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private Integer requestedRoleId;
    private String requestedRoleName;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private String requestType; // REGISTRATION, PASSWORD_RESET
    private Long reviewedBy;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    private String reviewNotes;
    private LocalDateTime createdAt;

    public AccountRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getRequestedRoleId() { return requestedRoleId; }
    public void setRequestedRoleId(Integer requestedRoleId) { this.requestedRoleId = requestedRoleId; }

    public String getRequestedRoleName() { return requestedRoleName; }
    public void setRequestedRoleName(String requestedRoleName) { this.requestedRoleName = requestedRoleName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
