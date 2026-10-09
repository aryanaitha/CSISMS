package com.csims.service.impl;

import com.csims.dao.AccountRequestDAO;
import com.csims.dao.PermissionDAO;
import com.csims.dao.UserDAO;
import com.csims.dao.impl.AccountRequestDAOImpl;
import com.csims.dao.impl.PermissionDAOImpl;
import com.csims.dao.impl.UserDAOImpl;
import com.csims.model.AccountRequest;
import com.csims.model.User;
import com.csims.security.*;
import com.csims.service.AuditService;
import com.csims.service.AuthService;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final UserDAO userDAO = new UserDAOImpl();
    private final PermissionDAO permissionDAO = new PermissionDAOImpl();
    private final AccountRequestDAO accountRequestDAO = new AccountRequestDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    public User login(String usernameOrEmail, char[] password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() || password == null || password.length == 0) {
            throw new AuthenticationException("Invalid username or password.");
        }

        String identifier = usernameOrEmail.trim();
        Optional<User> userOpt = userDAO.findByUsernameOrEmail(identifier);

        if (userOpt.isEmpty()) {
            auditService.log(identifier, "GUEST", "LOGIN_FAILURE", "AUTH", "USER", null, null, null, "FAILED", "Attempt for non-existent user identifier.");
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();

        // 1. Check account active status
        if (!user.isActive()) {
            auditService.log(user, "LOGIN_FAILED_DEACTIVATED", "AUTH", "USER", String.valueOf(user.getUserId()), null, null, "DENIED", "Deactivated account attempted login.");
            throw new AuthenticationException("Account is deactivated. Please contact your administrator.");
        }

        // 2. Check lockout status
        LocalDateTime now = LocalDateTime.now();
        if (user.getLockedUntil() != null) {
            if (user.getLockedUntil().isAfter(now)) {
                long remainingMinutes = Math.max(1, Duration.between(now, user.getLockedUntil()).toMinutes() + 1);
                auditService.log(user, "LOGIN_BLOCKED_LOCKED", "AUTH", "USER", String.valueOf(user.getUserId()), null, null, "BLOCKED", "Locked account attempted login.");
                throw new AccountLockedException("Account is locked due to too many failed attempts. Try again in " + remainingMinutes + " minute(s).",
                    user.getLockedUntil(), remainingMinutes);
            } else {
                // Lock has expired, reset failed attempts
                userDAO.resetLockout(user.getUserId());
                user.setFailedAttempts(0);
                user.setLockedUntil(null);
            }
        }

        // 3. Verify PBKDF2 password
        boolean valid = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());

        if (!valid) {
            int newFailedCount = user.getFailedAttempts() + 1;
            if (newFailedCount >= MAX_FAILED_ATTEMPTS) {
                LocalDateTime lockExpiration = now.plusMinutes(LOCKOUT_MINUTES);
                userDAO.lockAccount(user.getUserId(), lockExpiration, newFailedCount);
                auditService.log(user, "ACCOUNT_LOCKED", "AUTH", "USER", String.valueOf(user.getUserId()),
                    "attempts: " + user.getFailedAttempts(), "locked: 15 mins", "LOCKED", "Account locked for 15 minutes after 5 consecutive failed attempts.");
                throw new AccountLockedException("Too many failed login attempts. Account is locked for 15 minutes.", lockExpiration, LOCKOUT_MINUTES);
            } else {
                userDAO.updateFailedAttempts(user.getUserId(), newFailedCount);
                auditService.log(user, "LOGIN_FAILURE", "AUTH", "USER", String.valueOf(user.getUserId()),
                    null, null, "FAILED", "Incorrect password attempt (" + newFailedCount + "/" + MAX_FAILED_ATTEMPTS + ").");
                throw new AuthenticationException("Invalid username or password.");
            }
        }

        // 4. Success: reset failed attempts, update last login, load permissions
        userDAO.updateLastLogin(user.getUserId());
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(now);

        Set<String> permissions = permissionDAO.findPermissionCodesByRoleId(user.getRoleId());
        SessionContext.getInstance().setSession(user, permissions);

        auditService.log(user, "LOGIN_SUCCESS", "AUTH", "USER", String.valueOf(user.getUserId()),
            null, null, "SUCCESS", "User authenticated successfully.");

        return user;
    }

    @Override
    public boolean changePassword(int userId, char[] currentPassword, char[] newPassword, boolean forceChange) {
        if (newPassword == null || newPassword.length == 0) {
            throw new ValidationException("New password cannot be empty.");
        }

        String newPassStr = new String(newPassword);
        if (!PasswordUtil.meetsPolicy(newPassStr)) {
            throw new ValidationException("New password must be at least 8 characters long, contain at least one letter and at least one digit.");
        }

        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("User account not found.");
        }
        User user = userOpt.get();

        if (!forceChange) {
            if (currentPassword == null || currentPassword.length == 0) {
                throw new ValidationException("Current password is required.");
            }
            if (!PasswordUtil.verifyPassword(currentPassword, user.getSalt(), user.getPasswordHash())) {
                throw new ValidationException("Current password is incorrect.");
            }
        }

        // Check that new password is not identical to old password
        if (PasswordUtil.verifyPassword(newPassword, user.getSalt(), user.getPasswordHash())) {
            throw new ValidationException("New password cannot be the same as the current password.");
        }

        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);

        boolean updated = userDAO.updatePassword(userId, newHash, newSalt, false);
        if (updated) {
            user.setMustChangePassword(false);
            user.setPasswordHash(newHash);
            user.setSalt(newSalt);
            auditService.log(user, "PASSWORD_CHANGE", "AUTH", "USER", String.valueOf(userId),
                null, null, "SUCCESS", forceChange ? "Forced password change completed." : "User changed password.");
        }
        return updated;
    }

    @Override
    public int requestRegistration(AccountRequest request) {
        Validator.requireNonEmpty(request.getFullName(), "Full Name");
        Validator.validateEmail(request.getEmail(), "Email");
        Validator.validateUsername(request.getUsername(), "Username");
        if (request.getRequestedRoleId() == null || request.getRequestedRoleId() <= 0) {
            throw new ValidationException("Please select a valid requested role.");
        }
        if (request.getRequestedRoleId() == 1) { // Cannot request Administrator
            throw new ValidationException("Administrator role cannot be requested.");
        }

        // Check if username or email already exists in users
        if (userDAO.findByUsernameOrEmail(request.getUsername()).isPresent()) {
            throw new ValidationException("Username is already taken by an existing account.");
        }
        if (userDAO.findByUsernameOrEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("Email address is already registered.");
        }

        request.setRequestType("REGISTRATION");
        request.setStatus("PENDING");

        int reqId = accountRequestDAO.create(request);
        auditService.log(request.getUsername(), "APPLICANT", "ACCOUNT_REQUEST_CREATED", "ACCOUNT_REQUEST",
            "REQUEST", String.valueOf(reqId), null, null, "SUCCESS", "Submitted registration request for role ID " + request.getRequestedRoleId());
        return reqId;
    }

    @Override
    public int requestPasswordReset(String usernameOrEmail, String reason) {
        Validator.requireNonEmpty(usernameOrEmail, "Username or Email");

        String identifier = usernameOrEmail.trim();
        Optional<User> userOpt = userDAO.findByUsernameOrEmail(identifier);

        AccountRequest req = new AccountRequest();
        req.setRequestType("PASSWORD_RESET");
        req.setStatus("PENDING");
        req.setReason(reason != null ? reason.trim() : "Forgotten credentials");

        if (userOpt.isPresent()) {
            User u = userOpt.get();
            req.setFullName(u.getFullName());
            req.setEmail(u.getEmail());
            req.setUsername(u.getUsername());
            req.setRequestedRoleId(u.getRoleId());
        } else {
            // Keep record even if account is not resolved yet, or generate synthetic username
            req.setFullName("User (" + identifier + ")");
            req.setEmail(identifier.contains("@") ? identifier : identifier + "@unknown.local");
            req.setUsername(identifier);
        }

        int reqId = accountRequestDAO.create(req);
        auditService.log(identifier, "USER", "PASSWORD_RESET_REQUESTED", "ACCOUNT_REQUEST",
            "REQUEST", String.valueOf(reqId), null, null, "SUCCESS", "Submitted password reset request.");
        return reqId;
    }

    @Override
    public void logout() {
        User user = SessionContext.getInstance().getCurrentUser();
        if (user != null) {
            auditService.log(user, "LOGOUT", "AUTH", "USER", String.valueOf(user.getUserId()),
                null, null, "SUCCESS", "User logged out.");
        }
        IdleTimeoutManager.getInstance().stop();
        SessionContext.getInstance().clear();
    }
}
