package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.AccountRequest;
import com.csims.model.Permission;
import com.csims.model.Role;
import com.csims.model.User;
import com.csims.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Consolidated Administration Service.
 * Handles User Management, Roles & Permissions, and Account Registration Requests.
 */
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuditService auditService,
                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    // =========================================================================
    // SECTION 1: USERS
    // =========================================================================

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Long createUser(User user, String rawPassword, Long operatorId, String operatorUsername) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ApiException("Username already exists", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ApiException("Email already exists", HttpStatus.CONFLICT);
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setMustChangePassword(true);
        user.setActive(true);

        Long createdId = userRepository.createUser(user);
        auditService.log(operatorId, operatorUsername, null, "USER_CREATE", "USER", "USER", String.valueOf(createdId), null, user.getUsername(), "SUCCESS", "Created new user " + user.getUsername());
        return createdId;
    }

    @Transactional
    public void updateUser(Long id, User updateData, Long operatorId, String operatorUsername) {
        User existing = getUserById(id);
        
        if (!existing.getEmail().equalsIgnoreCase(updateData.getEmail()) && userRepository.existsByEmail(updateData.getEmail())) {
            throw new ApiException("Email already exists", HttpStatus.CONFLICT);
        }

        existing.setFullName(updateData.getFullName());
        existing.setEmail(updateData.getEmail());
        existing.setRoleId(updateData.getRoleId());
        existing.setActive(updateData.isActive());

        userRepository.updateUser(existing);
        auditService.log(operatorId, operatorUsername, null, "USER_UPDATE", "USER", "USER", String.valueOf(id), null, existing.getUsername(), "SUCCESS", "Updated user details for " + existing.getUsername());
    }

    @Transactional
    public void toggleUserStatus(Long id, boolean active, Long operatorId, String operatorUsername) {
        User existing = getUserById(id);
        if (existing.getId() == 1 && !active) {
            throw new ApiException("Cannot deactivate primary Administrator account", HttpStatus.BAD_REQUEST);
        }

        existing.setActive(active);
        userRepository.updateUser(existing);
        String action = active ? "USER_ACTIVATE" : "USER_DEACTIVATE";
        auditService.log(operatorId, operatorUsername, null, action, "USER", "USER", String.valueOf(id), null, String.valueOf(active), "SUCCESS", (active ? "Activated" : "Deactivated") + " user " + existing.getUsername());
    }

    @Transactional
    public void resetPassword(Long id, String newPassword, Long operatorId, String operatorUsername) {
        User existing = getUserById(id);
        String encoded = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(id, encoded, true);
        userRepository.resetFailedLoginsAndLock(id);

        auditService.log(operatorId, operatorUsername, null, "PASSWORD_RESET_ADMIN", "USER", "USER", String.valueOf(id), null, null, "SUCCESS", "Admin reset password for user " + existing.getUsername());
    }

    // =========================================================================
    // SECTION 2: ROLES & PERMISSIONS
    // =========================================================================

    public List<Role> getAllRoles() {
        return userRepository.findAllRoles();
    }

    public Role getRoleById(Integer id) {
        return userRepository.findRoleById(id)
                .orElseThrow(() -> new ApiException("Role not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    public List<Permission> getAllPermissions() {
        return userRepository.findAllPermissions();
    }

    public List<Permission> getPermissionsByRoleId(Integer roleId) {
        getRoleById(roleId); // validate existence
        return userRepository.findPermissionsByRoleId(roleId);
    }

    @Transactional
    public void updateRolePermissions(Integer roleId, List<Integer> permissionIds, Long operatorId, String operatorUsername) {
        Role role = getRoleById(roleId);
        userRepository.updateRolePermissions(roleId, permissionIds);
        auditService.log(operatorId, operatorUsername, null, "ROLE_PERMISSIONS_UPDATE", "ROLE", "ROLE", String.valueOf(roleId), null, null, "SUCCESS", "Updated permissions for role " + role.getName());
    }

    // =========================================================================
    // SECTION 3: ACCOUNT REQUESTS
    // =========================================================================

    public List<AccountRequest> getAccountRequests(String status) {
        return userRepository.findAllAccountRequests(status);
    }

    public AccountRequest getRequestById(Long id) {
        return userRepository.findAccountRequestById(id)
                .orElseThrow(() -> new ApiException("Account request not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void approveRequest(Long id, Long reviewerId, String reviewerUsername, String reviewNotes) {
        AccountRequest req = getRequestById(id);
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new ApiException("Request has already been processed", HttpStatus.BAD_REQUEST);
        }

        if ("REGISTRATION".equalsIgnoreCase(req.getRequestType())) {
            if (userRepository.existsByUsername(req.getUsername())) {
                throw new ApiException("Username is already taken by another user", HttpStatus.CONFLICT);
            }
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new ApiException("Email is already registered to another user", HttpStatus.CONFLICT);
            }

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

        userRepository.updateAccountRequestReview(id, "APPROVED", reviewerId, reviewNotes);
        auditService.log(reviewerId, reviewerUsername, null, "ACCOUNT_REQUEST_APPROVE", "ACCOUNT_REQUEST", "ACCOUNT_REQUEST", String.valueOf(id), "PENDING", "APPROVED", "SUCCESS", "Approved " + req.getRequestType() + " for " + req.getUsername());
    }

    @Transactional
    public void rejectRequest(Long id, Long reviewerId, String reviewerUsername, String reviewNotes) {
        AccountRequest req = getRequestById(id);
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new ApiException("Request has already been processed", HttpStatus.BAD_REQUEST);
        }

        userRepository.updateAccountRequestReview(id, "REJECTED", reviewerId, reviewNotes);
        auditService.log(reviewerId, reviewerUsername, null, "ACCOUNT_REQUEST_REJECT", "ACCOUNT_REQUEST", "ACCOUNT_REQUEST", String.valueOf(id), "PENDING", "REJECTED", "SUCCESS", "Rejected " + req.getRequestType() + " for " + req.getUsername() + ". Reason: " + reviewNotes);
    }
}
