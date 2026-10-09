package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.AccountRequest;
import com.csims.security.CustomUserDetails;
import com.csims.service.AccountRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account-requests")
public class AccountRequestController {
    private final AccountRequestService accountRequestService;

    public AccountRequestController(AccountRequestService accountRequestService) {
        this.accountRequestService = accountRequestService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNT_REQUEST_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<AccountRequest>>> getAccountRequests(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(accountRequestService.getAccountRequests(status)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ACCOUNT_REQUEST_REVIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> approveRequest(@PathVariable Long id,
                                                            @RequestBody(required = false) Map<String, String> body,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String notes = body != null && body.containsKey("reviewNotes") ? body.get("reviewNotes") : "Approved by administrator";
        accountRequestService.approveRequest(id, userDetails.getId(), userDetails.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.ok("Request approved successfully", null));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ACCOUNT_REQUEST_REVIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(@PathVariable Long id,
                                                           @RequestBody(required = false) Map<String, String> body,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        String notes = body != null && body.containsKey("reviewNotes") ? body.get("reviewNotes") : "Rejected by administrator";
        accountRequestService.rejectRequest(id, userDetails.getId(), userDetails.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.ok("Request rejected", null));
    }
}
