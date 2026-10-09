package com.csims.dao;

import com.csims.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(int userId);
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    List<User> findAll();
    List<User> search(String keyword, Integer roleId, Boolean activeOnly);
    int create(User user);
    boolean update(User user);
    boolean updatePassword(int userId, String passwordHash, String salt, boolean mustChangePassword);
    boolean updateFailedAttempts(int userId, int failedAttempts);
    boolean lockAccount(int userId, java.time.LocalDateTime lockedUntil, int failedAttempts);
    boolean resetLockout(int userId);
    boolean updateLastLogin(int userId);
    boolean updateStatus(int userId, boolean isActive);
    boolean delete(int userId);
}
