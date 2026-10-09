package com.csims.service.impl;

import com.csims.dao.AccountRequestDAO;
import com.csims.dao.PermissionDAO;
import com.csims.dao.RoleDAO;
import com.csims.dao.UserDAO;
import com.csims.dao.impl.AccountRequestDAOImpl;
import com.csims.dao.impl.PermissionDAOImpl;
import com.csims.dao.impl.RoleDAOImpl;
import com.csims.dao.impl.UserDAOImpl;
import com.csims.model.AccountRequest;
import com.csims.model.Permission;
import com.csims.model.Role;
import com.csims.model.User;
import com.csims.security.PasswordUtil;
import com.csims.security.PermissionChecker;
import com.csims.security.SessionContext;
import com.csims.service.AuditService;
import com.csims.service.NotificationService;
import com.csims.service.UserService;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO = new UserDAOImpl();
    private final RoleDAO roleDAO = new RoleDAOImpl();
    private final PermissionDAO permissionDAO = new PermissionDAOImpl();
    private final AccountRequestDAO requestDAO = new AccountRequestDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final NotificationService notificationService = new NotificationServiceImpl();

    @Override
    public List<User> getAllUsers() {
        PermissionChecker.require("USER_VIEW");
        return userDAO.findAll();
    }

    @Override
    public List<User> searchUsers(String keyword, Integer roleId, Boolean activeOnly) {
        PermissionChecker.require("USER_VIEW");
        return userDAO.search(keyword, roleId, activeOnly);
    }

    @Override
    public User getUserById(int userId) {
        PermissionChecker.require("USER_VIEW");
        return userDAO.findById(userId)
            .orElseThrow(() -> new ValidationException("User #" + userId + " not found."));
    }

    @Override
    public int createUser(User user, String tempPassword) {
        PermissionChecker.require("USER_CREATE");
        Validator.requireNonEmpty(user.getFullName(), "Full Name");
        Validator.validateUsername(user.getUsername(), "Username");
        Validator.validateEmail(user.getEmail(), "Email");
        Validator.requireNonEmpty(tempPassword, "Temporary Password");

        if (!PasswordUtil.meetsPolicy(tempPassword)) {
            throw new ValidationException("Temporary password must be at least 8 chars with 1 letter and 1 digit.");
        }

        if (userDAO.findByUsernameOrEmail(user.getUsername()).isPresent()) {
            throw new ValidationException("Username is already in use.");
        }
        if (userDAO.findByUsernameOrEmail(user.getEmail()).isPresent()) {
            throw new ValidationException("Email is already in use.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(tempPassword, salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        user.setMustChangePassword(true);

        int id = userDAO.create(user);
        auditService.logCurrent("USER_CREATE", "USER", "USER", String.valueOf(id), null, user.getUsername(), "SUCCESS", "Admin created user account.");
        return id;
    }

    @Override
    public boolean updateUser(User user) {
        PermissionChecker.require("USER_UPDATE");
        Validator.requireNonEmpty(user.getFullName(), "Full Name");
        Validator.validateEmail(user.getEmail(), "Email");

        User existing = getUserById(user.getUserId());
        String oldVal = existing.getFullName() + ", role: " + existing.getRoleId();
        String newVal = user.getFullName() + ", role: " + user.getRoleId();

        boolean updated = userDAO.update(user);
        if (updated) {
            auditService.logCurrent("USER_UPDATE", "USER", "USER", String.valueOf(user.getUserId()), oldVal, newVal, "SUCCESS", "Updated user details.");
        }
        return updated;
    }

    @Override
    public boolean setUserActive(int userId, boolean isActive) {
        PermissionChecker.require("USER_STATUS");
        User user = getUserById(userId);
        boolean changed = userDAO.updateStatus(userId, isActive);
        if (changed) {
            String act = isActive ? "USER_ACTIVATED" : "USER_DEACTIVATED";
            auditService.logCurrent(act, "USER", "USER", String.valueOf(userId),
                "active: " + user.isActive(), "active: " + isActive, "SUCCESS", "Changed active status.");
        }
        return changed;
    }

    @Override
    public boolean adminResetPassword(int userId, String tempPassword) {
        PermissionChecker.require("USER_PASSWORD_RESET");
        Validator.requireNonEmpty(tempPassword, "Temporary Password");
        if (!PasswordUtil.meetsPolicy(tempPassword)) {
            throw new ValidationException("Temporary password must be at least 8 characters with 1 letter and 1 digit.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(tempPassword, salt);
        boolean reset = userDAO.updatePassword(userId, hash, salt, true);
        if (reset) {
            userDAO.resetLockout(userId);
            auditService.logCurrent("PASSWORD_RESET", "USER", "USER", String.valueOf(userId),
                null, null, "SUCCESS", "Admin reset user password (must change on next login).");
        }
        return reset;
    }

    @Override
    public boolean unlockUserAccount(int userId) {
        PermissionChecker.require("USER_STATUS");
        boolean unlocked = userDAO.resetLockout(userId);
        if (unlocked) {
            auditService.logCurrent("ACCOUNT_UNLOCKED", "USER", "USER", String.valueOf(userId),
                null, null, "SUCCESS", "Admin unlocked account lockout.");
        }
        return unlocked;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleDAO.findAll();
    }

    @Override
    public List<Permission> getAllPermissions() {
        PermissionChecker.require("ROLE_VIEW");
        return permissionDAO.findAll();
    }

    @Override
    public List<Permission> getPermissionsForRole(int roleId) {
        PermissionChecker.require("ROLE_VIEW");
        return permissionDAO.findByRoleId(roleId);
    }

    @Override
    public boolean updateRolePermissions(int roleId, Set<Integer> permissionIds) {
        PermissionChecker.require("ROLE_UPDATE");
        boolean updated = permissionDAO.updateRolePermissions(roleId, permissionIds);
        if (updated) {
            auditService.logCurrent("ROLE_PERMISSIONS_UPDATED", "ROLE", "ROLE", String.valueOf(roleId),
                null, "permissions: " + (permissionIds != null ? permissionIds.size() : 0), "SUCCESS", "Admin modified permissions for role #" + roleId);
        }
        return updated;
    }

    @Override
    public List<AccountRequest> getAccountRequests(String keyword, String type, String status) {
        PermissionChecker.require("ACCOUNT_REQUEST_VIEW");
        return requestDAO.search(keyword, type, status);
    }

    @Override
    public boolean approveRequest(int requestId, String tempPassword, String reviewNote) {
        PermissionChecker.require("ACCOUNT_REQUEST_REVIEW");
        AccountRequest req = requestDAO.findById(requestId)
            .orElseThrow(() -> new ValidationException("Request #" + requestId + " not found."));

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new ValidationException("Only PENDING requests can be approved.");
        }

        User reviewer = SessionContext.getInstance().getCurrentUser();
        int reviewerId = reviewer != null ? reviewer.getUserId() : 1;

        if ("REGISTRATION".equalsIgnoreCase(req.getRequestType())) {
            Validator.requireNonEmpty(tempPassword, "Temporary Password");
            if (!PasswordUtil.meetsPolicy(tempPassword)) {
                throw new ValidationException("Temporary password must be at least 8 chars with 1 letter and 1 digit.");
            }

            // Create the new user
            User newUser = new User();
            newUser.setUsername(req.getUsername());
            newUser.setEmail(req.getEmail());
            newUser.setFullName(req.getFullName());
            newUser.setRoleId(req.getRequestedRoleId() != null ? req.getRequestedRoleId() : 8);
            newUser.setActive(true);
            newUser.setMustChangePassword(true);

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(tempPassword, salt);
            newUser.setSalt(salt);
            newUser.setPasswordHash(hash);

            int userId = userDAO.create(newUser);
            requestDAO.updateReview(requestId, "APPROVED", reviewerId, reviewNote != null ? reviewNote : "Registration approved with temporary credentials.");

            auditService.logCurrent("ACCOUNT_REQUEST_APPROVED", "ACCOUNT_REQUEST", "USER", String.valueOf(userId),
                null, newUser.getUsername(), "SUCCESS", "Approved registration for " + newUser.getUsername());
            return true;

        } else if ("PASSWORD_RESET".equalsIgnoreCase(req.getRequestType())) {
            Validator.requireNonEmpty(tempPassword, "Temporary Password");
            if (!PasswordUtil.meetsPolicy(tempPassword)) {
                throw new ValidationException("Temporary password must be at least 8 chars with 1 letter and 1 digit.");
            }

            Optional<User> uOpt = userDAO.findByUsernameOrEmail(req.getUsername());
            if (uOpt.isEmpty()) {
                uOpt = userDAO.findByUsernameOrEmail(req.getEmail());
            }

            if (uOpt.isPresent()) {
                User u = uOpt.get();
                String salt = PasswordUtil.generateSalt();
                String hash = PasswordUtil.hashPassword(tempPassword, salt);
                userDAO.updatePassword(u.getUserId(), hash, salt, true);
                userDAO.resetLockout(u.getUserId());
                auditService.logCurrent("PASSWORD_RESET", "ACCOUNT_REQUEST", "USER", String.valueOf(u.getUserId()),
                    null, null, "SUCCESS", "Approved password reset for " + u.getUsername());
            }

            requestDAO.updateReview(requestId, "APPROVED", reviewerId, reviewNote != null ? reviewNote : "Password reset approved with temporary password.");
            return true;
        }

        return false;
    }

    @Override
    public boolean rejectRequest(int requestId, String reviewNote) {
        PermissionChecker.require("ACCOUNT_REQUEST_REVIEW");
        Validator.requireNonEmpty(reviewNote, "Rejection Reason / Note");

        AccountRequest req = requestDAO.findById(requestId)
            .orElseThrow(() -> new ValidationException("Request #" + requestId + " not found."));

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new ValidationException("Only PENDING requests can be rejected.");
        }

        User reviewer = SessionContext.getInstance().getCurrentUser();
        int reviewerId = reviewer != null ? reviewer.getUserId() : 1;

        boolean rejected = requestDAO.updateReview(requestId, "REJECTED", reviewerId, reviewNote);
        if (rejected) {
            auditService.logCurrent("ACCOUNT_REQUEST_REJECTED", "ACCOUNT_REQUEST", "REQUEST", String.valueOf(requestId),
                "PENDING", "REJECTED", "SUCCESS", "Rejected request: " + reviewNote);
        }
        return rejected;
    }
}
