package com.csims.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Access registration or password reset request.
 */
public class AccountRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int requestId;
    private String requestType; // REGISTRATION or PASSWORD_RESET
    private String fullName;
    private String email;
    private String username;
    private Integer requestedRoleId;
    private String reason;
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    private Integer reviewedBy;
    private String reviewNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    // Joined fields
    private String requestedRoleName;
    private String reviewerUsername;

    public AccountRequest() {}

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRequestedRoleId() {
        return requestedRoleId;
    }

    public void setRequestedRoleId(Integer requestedRoleId) {
        this.requestedRoleId = requestedRoleId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRequestedRoleName() {
        return requestedRoleName;
    }

    public void setRequestedRoleName(String requestedRoleName) {
        this.requestedRoleName = requestedRoleName;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
    }
}
