package com.csims.service;

import com.csims.dto.ApiResponse;
import com.csims.exception.ApiException;
import com.csims.model.User;
import com.csims.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

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
}
