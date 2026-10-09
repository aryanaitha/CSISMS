package com.csims.security;

import com.csims.model.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Thread-safe singleton managing the active user session and security permissions.
 */
public final class SessionContext {

    private static volatile SessionContext instance;

    private User currentUser;
    private final Set<String> permissions = new HashSet<>();

    private SessionContext() {}

    public static SessionContext getInstance() {
        if (instance == null) {
            synchronized (SessionContext.class) {
                if (instance == null) {
                    instance = new SessionContext();
                }
            }
        }
        return instance;
    }

    public synchronized void setSession(User user, Set<String> perms) {
        this.currentUser = user;
        this.permissions.clear();
        if (perms != null) {
            this.permissions.addAll(perms);
        }
    }

    public synchronized void clear() {
        this.currentUser = null;
        this.permissions.clear();
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public synchronized boolean hasPermission(String permissionCode) {
        if (currentUser == null || permissionCode == null) {
            return false;
        }
        // Admin role (roleId == 1) has all permissions by default
        if (currentUser.getRoleId() == 1) {
            return true;
        }
        return permissions.contains(permissionCode.trim().toUpperCase());
    }

    public synchronized boolean hasAnyPermission(String... codes) {
        for (String code : codes) {
            if (hasPermission(code)) {
                return true;
            }
        }
        return false;
    }
}
