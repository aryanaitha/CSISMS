package com.csims.security;

import com.csims.model.User;

/**
 * RBAC Permission evaluator for service layer enforcement.
 */
public final class PermissionChecker {

    private PermissionChecker() {}

    /**
     * Checks if the active session has the required permission.
     */
    public static boolean has(String permissionCode) {
        return SessionContext.getInstance().hasPermission(permissionCode);
    }

    /**
     * Checks if the given user has the permission (via current session context).
     */
    public static boolean has(User user, String permissionCode) {
        if (user == null || permissionCode == null) {
            return false;
        }
        if (user.getRoleId() == 1) { // Administrator
            return true;
        }
        return SessionContext.getInstance().hasPermission(permissionCode);
    }

    /**
     * Requires the specified permission, throwing UnauthorizedException if absent.
     */
    public static void require(String permissionCode) {
        if (!has(permissionCode)) {
            throw new UnauthorizedException("Access Denied: Missing required permission [" + permissionCode + "]", permissionCode);
        }
    }

    /**
     * Requires the specified permission for the specified user, throwing UnauthorizedException if absent.
     */
    public static void require(User user, String permissionCode) {
        if (!has(user, permissionCode)) {
            throw new UnauthorizedException("Access Denied: User '" + (user != null ? user.getUsername() : "anonymous") +
                                            "' lacks required permission [" + permissionCode + "]", permissionCode);
        }
    }
}
