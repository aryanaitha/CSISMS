package com.csims.security;

import com.csims.service.ServiceException;

/**
 * Thrown when user authentication fails (invalid credentials, deactivated account, etc.).
 */
public class AuthenticationException extends ServiceException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
