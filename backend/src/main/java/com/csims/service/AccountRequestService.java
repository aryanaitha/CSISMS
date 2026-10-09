package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.AccountRequest;
import com.csims.model.User;
import com.csims.repository.AccountRequestRepository;
import com.csims.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountRequestService {
    private final AccountRequestRepository accountRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public AccountRequestService(AccountRequestRepository accountRequestRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuditService auditService,
                                 NotificationService notificationService) {
        this.accountRequestRepository = accountRequestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public List<AccountRequest> getAccountRequests(String status) {
        return accountRequestRepository.findAll(status);
    }

    public AccountRequest getRequestById(Long id) {
        return accountRequestRepository.findById(id)
                .orElseThrow(() -> new ApiException("Account request not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void approveRequest(Long id, Long reviewerId, String reviewerUsername, String reviewNotes) {
        AccountRequest req = getRequestById(id);
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new ApiException("Request has already been processed", HttpStatus.BAD_REQUEST);
        }

        if ("REGISTRATION".equalsIgnoreCase(req.getRequestType())) {
            // Check if username/email already taken
            if (userRepository.existsByUsername(req.getUsername())) {
                throw new ApiException("Username is already taken by another user", HttpStatus.CONFLICT);
            }
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new ApiException("Email is already registered to another user", HttpStatus.CONFLICT);
            }

            // Create new user with temp password
            User u = new User();
            u.setUsername(req.getUsername());
            u.setEmail(req.getEmail());
            u.setFullName(req.getFullName());
            u.setRoleId(req.getRequestedRoleId() != null ? req.getRequestedRoleId() : 8);
            u.setPasswordHash(passwordEncoder.encode("Welcome@123"));
            u.setActive(true);
            u.setMustChangePassword(true);

            Long createdUserId = userRepository.createUser(u);
            notificationService.notify(createdUserId, "Account Approved", "Your account registration has been approved. Your temporary password is Welcome@123. You will be prompted to change it upon first login.", "SUCCESS", null);
        } else if ("PASSWORD_RESET".equalsIgnoreCase(req.getRequestType())) {
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(req.getUsername());
            if (userOpt.isPresent()) {
                User u = userOpt.get();
                userRepository.updatePassword(u.getId(), passwordEncoder.encode("Reset@12345"), true);
                userRepository.resetFailedLoginsAndLock(u.getId());
                notificationService.notify(u.getId(), "Password Reset Approved", "Your password has been reset to Reset@12345. You must set a new password on your next login.", "INFO", null);
            }
        }

        accountRequestRepository.updateReview(id, "APPROVED", reviewerId, reviewNotes);
        auditService.log(reviewerId, reviewerUsername, null, "ACCOUNT_REQUEST_APPROVE", "ACCOUNT_REQUEST", "ACCOUNT_REQUEST", String.valueOf(id), "PENDING", "APPROVED", "SUCCESS", "Approved " + req.getRequestType() + " for " + req.getUsername());
    }

    @Transactional
    public void rejectRequest(Long id, Long reviewerId, String reviewerUsername, String reviewNotes) {
        AccountRequest req = getRequestById(id);
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new ApiException("Request has already been processed", HttpStatus.BAD_REQUEST);
        }

        accountRequestRepository.updateReview(id, "REJECTED", reviewerId, reviewNotes);
        auditService.log(reviewerId, reviewerUsername, null, "ACCOUNT_REQUEST_REJECT", "ACCOUNT_REQUEST", "ACCOUNT_REQUEST", String.valueOf(id), "PENDING", "REJECTED", "SUCCESS", "Rejected " + req.getRequestType() + " for " + req.getUsername() + ". Reason: " + reviewNotes);
    }
}
