package com.csims.security;

import com.csims.service.ServiceException;

/**
 * Thrown when an authenticated user attempts an operation lacking required RBAC permission.
 */
public class UnauthorizedException extends ServiceException {

    private final String requiredPermission;

    public UnauthorizedException(String message) {
        super(message);
        this.requiredPermission = null;
    }

    public UnauthorizedException(String message, String requiredPermission) {
        super(message);
        this.requiredPermission = requiredPermission;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }
}
