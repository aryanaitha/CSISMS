package com.csims.service;

import com.csims.model.AccountRequest;
import com.csims.model.Permission;
import com.csims.model.Role;
import com.csims.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<User> getAllUsers();
    List<User> searchUsers(String keyword, Integer roleId, Boolean activeOnly);
    User getUserById(int userId);
    int createUser(User user, String tempPassword);
    boolean updateUser(User user);
    boolean setUserActive(int userId, boolean isActive);
    boolean adminResetPassword(int userId, String tempPassword);
    boolean unlockUserAccount(int userId);

    List<Role> getAllRoles();
    List<Permission> getAllPermissions();
    List<Permission> getPermissionsForRole(int roleId);
    boolean updateRolePermissions(int roleId, Set<Integer> permissionIds);

    List<AccountRequest> getAccountRequests(String keyword, String type, String status);
    boolean approveRequest(int requestId, String tempPassword, String reviewNote);
    boolean rejectRequest(int requestId, String reviewNote);
}
