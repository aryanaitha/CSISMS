package com.csims.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Workforce worker entity.
 */
public class Worker implements Serializable {
    private static final long serialVersionUID = 1L;

    private int workerId;
    private String fullName;
    private String email;
    private String phone;
    private String trade;
    private Integer contractorId;
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, ON_LEAVE
    private LocalDate joinedDate;

    // Joined fields
    private String contractorName;

    public Worker() {}

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public Integer getContractorId() {
        return contractorId;
    }

    public void setContractorId(Integer contractorId) {
        this.contractorId = contractorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
    }

    @Override
    public String toString() {
        return fullName + " (" + trade + ")";
    }
}
