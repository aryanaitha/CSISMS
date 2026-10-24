package com.csims.service;

import com.csims.dto.Dtos.*;
import com.csims.dto.ApiResponse;
import com.csims.exception.AppExceptions.ApiException;
import com.csims.model.Models.AccountRequest;
import com.csims.model.Models.User;
import com.csims.repository.UserRepository;
import com.csims.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final SystemService systemService;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern LETTER_PATTERN = Pattern.compile(".*[a-zA-Z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");

    public AuthService(UserRepository userRepository,
                       SystemService systemService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.systemService = systemService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto login(LoginRequest req, HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(req.getUsername());

        if (userOpt.isEmpty()) {
            systemService.log(null, req.getUsername(), null, "LOGIN_FAILED", "AUTH", "USER", null, null, null, "FAILURE", "User not found");
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            systemService.log(user.getId(), user.getUsername(), user.getRoleName(), "LOGIN_FAILED", "AUTH", "USER", String.valueOf(user.getId()), null, null, "FAILURE", "Account deactivated");
            throw new DisabledException("Account is deactivated. Please contact an administrator.");
        }

        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            long remainingMins = Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
            systemService.log(user.getId(), user.getUsername(), user.getRoleName(), "LOGIN_FAILED", "AUTH", "USER", String.valueOf(user.getId()), null, null, "FAILURE", "Attempt while locked");
            throw new LockedException("Account is temporarily locked. Try again in " + remainingMins + " minute(s).");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            userRepository.incrementFailedLogins(user.getId());
            int newAttempts = user.getFailedLoginAttempts() + 1;

            if (newAttempts >= 5) {
                LocalDateTime lockTime = LocalDateTime.now().plusMinutes(15);
                userRepository.lockAccount(user.getId(), lockTime);
                systemService.log(user.getId(), user.getUsername(), user.getRoleName(), "ACCOUNT_LOCKOUT", "AUTH", "USER", String.valueOf(user.getId()), null, null, "SUCCESS", "Account locked for 15 minutes after 5 failed attempts");
                throw new LockedException("Account locked for 15 minutes due to 5 consecutive failed login attempts.");
            } else {
                systemService.log(user.getId(), user.getUsername(), user.getRoleName(), "LOGIN_FAILED", "AUTH", "USER", String.valueOf(user.getId()), null, null, "FAILURE", "Incorrect password (attempt " + newAttempts + "/5)");
                throw new BadCredentialsException("Invalid username or password");
            }
        }

        // Credentials are valid
        userRepository.resetFailedLoginsAndLock(user.getId());
        userRepository.updateLastLogin(user.getId());

        Set<String> permissions = userRepository.findPermissionNamesByRoleId(user.getRoleId());
        CustomUserDetails userDetails = new CustomUserDetails(user, permissions);

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        systemService.log(user.getId(), user.getUsername(), user.getRoleName(), "LOGIN_SUCCESS", "AUTH", "USER", String.valueOf(user.getId()), null, null, "SUCCESS", "User logged in successfully");

        return toDto(user, permissions);
    }

    public void logout(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            systemService.log(userDetails.getId(), userDetails.getUsername(), userDetails.getRoleName(), "LOGOUT", "AUTH", "USER", String.valueOf(userDetails.getId()), null, null, "SUCCESS", "User logged out");
        }
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public UserDto getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ApiException("Not authenticated", HttpStatus.UNAUTHORIZED);
        }

        // Re-read fresh user data
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Set<String> permissions = userRepository.findPermissionNamesByRoleId(user.getRoleId());
        return toDto(user, permissions);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        validatePasswordPolicy(req.getNewPassword());

        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new ApiException("New password must be different from current password", HttpStatus.BAD_REQUEST);
        }

        String newHash = passwordEncoder.encode(req.getNewPassword());
        userRepository.updatePassword(userId, newHash, false);

        systemService.log(user.getId(), user.getUsername(), user.getRoleName(), "PASSWORD_CHANGE", "AUTH", "USER", String.valueOf(user.getId()), null, null, "SUCCESS", "Password updated successfully");
    }

    public void registerRequest(RegisterRequestDto req, HttpServletRequest request) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ApiException("Username is already taken", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException("Email is already registered", HttpStatus.CONFLICT);
        }

        // Cannot request Administrator role (id 1)
        if (req.getRequestedRoleId() != null && req.getRequestedRoleId() == 1) {
            throw new ApiException("Administrator role cannot be requested directly", HttpStatus.BAD_REQUEST);
        }

        AccountRequest ar = new AccountRequest();
        ar.setFullName(req.getFullName());
        ar.setEmail(req.getEmail());
        ar.setUsername(req.getUsername());
        ar.setRequestedRoleId(req.getRequestedRoleId());
        ar.setReason(req.getReason());
        ar.setStatus("PENDING");
        ar.setRequestType("REGISTRATION");

        Long id = userRepository.createAccountRequest(ar);
        systemService.log(null, req.getUsername(), null, "REGISTRATION_REQUEST", "AUTH", "ACCOUNT_REQUEST", String.valueOf(id), null, null, "SUCCESS", "New account registration request submitted");
    }

    public void forgotPassword(ForgotPasswordRequest req, HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(req.getUsernameOrEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            AccountRequest ar = new AccountRequest();
            ar.setFullName(user.getFullName());
            ar.setEmail(user.getEmail());
            ar.setUsername(user.getUsername());
            ar.setRequestedRoleId(user.getRoleId());
            ar.setReason("Password reset requested by user");
            ar.setStatus("PENDING");
            ar.setRequestType("PASSWORD_RESET");

            Long id = userRepository.createAccountRequest(ar);
            systemService.log(user.getId(), user.getUsername(), user.getRoleName(), "PASSWORD_RESET_REQUEST", "AUTH", "ACCOUNT_REQUEST", String.valueOf(id), null, null, "SUCCESS", "Password reset request filed");
        } else {
            systemService.log(null, req.getUsernameOrEmail(), null, "PASSWORD_RESET_ATTEMPT", "AUTH", "USER", null, null, null, "FAILURE", "Password reset attempted for non-existent user");
        }
    }

    private void validatePasswordPolicy(String password) {
        if (password == null || password.length() < 8) {
            throw new ApiException("Password must be at least 8 characters long", HttpStatus.BAD_REQUEST);
        }
        if (!LETTER_PATTERN.matcher(password).matches()) {
            throw new ApiException("Password must contain at least one letter", HttpStatus.BAD_REQUEST);
        }
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new ApiException("Password must contain at least one digit", HttpStatus.BAD_REQUEST);
        }
    }

    private UserDto toDto(User user, Set<String> permissions) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoleId(user.getRoleId());
        dto.setRoleName(user.getRoleName());
        dto.setActive(user.isActive());
        dto.setMustChangePassword(user.isMustChangePassword());
        dto.setLastLogin(user.getLastLogin());
        dto.setPermissions(permissions);
        return dto;
    }
}
