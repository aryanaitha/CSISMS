package com.csims.model;

import java.time.LocalDate;

public class Worker {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private String trade;
    private Integer contractorId;
    private String contractorName;
    private String status; // ACTIVE, INACTIVE
    private LocalDate joinedDate;

    public Worker() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTrade() { return trade; }
    public void setTrade(String trade) { this.trade = trade; }

    public Integer getContractorId() { return contractorId; }
    public void setContractorId(Integer contractorId) { this.contractorId = contractorId; }

    public String getContractorName() { return contractorName; }
    public void setContractorName(String contractorName) { this.contractorName = contractorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }
}
