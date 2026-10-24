package com.csims.service;

import com.csims.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final AdminService adminService;

    public UserService(AdminService adminService) {
        this.adminService = adminService;
    }

    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    public User getUserById(Long id) {
        return adminService.getUserById(id);
    }

    public Long createUser(User user, String rawPassword, Long operatorId, String operatorUsername) {
        return adminService.createUser(user, rawPassword, operatorId, operatorUsername);
    }

    public void updateUser(Long id, User updateData, Long operatorId, String operatorUsername) {
        adminService.updateUser(id, updateData, operatorId, operatorUsername);
    }

    public void toggleUserStatus(Long id, boolean active, Long operatorId, String operatorUsername) {
        adminService.toggleUserStatus(id, active, operatorId, operatorUsername);
    }

    public void resetPassword(Long id, String newPassword, Long operatorId, String operatorUsername) {
        adminService.resetPassword(id, newPassword, operatorId, operatorUsername);
    }
}
