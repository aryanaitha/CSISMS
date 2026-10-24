package com.csims.service;

import com.csims.model.AccountRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountRequestService {
    private final AdminService adminService;

    public AccountRequestService(AdminService adminService) {
        this.adminService = adminService;
    }

    public List<AccountRequest> getAccountRequests(String status) {
        return adminService.getAccountRequests(status);
    }

    public AccountRequest getRequestById(Long id) {
        return adminService.getRequestById(id);
    }

    public void approveRequest(Long id, Long reviewerId, String reviewerUsername, String reviewNotes) {
        adminService.approveRequest(id, reviewerId, reviewerUsername, reviewNotes);
    }

    public void rejectRequest(Long id, Long reviewerId, String reviewerUsername, String reviewNotes) {
        adminService.rejectRequest(id, reviewerId, reviewerUsername, reviewNotes);
    }
}
