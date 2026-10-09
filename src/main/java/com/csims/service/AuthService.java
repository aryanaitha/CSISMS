package com.csims.service;

import com.csims.model.AccountRequest;
import com.csims.model.User;

public interface AuthService {

    /**
     * Authenticates user against credentials.
     * Enforces active check, lockout policy, PBKDF2 verification, and loads RBAC session.
     *
     * @param usernameOrEmail user identifier
     * @param password raw password characters
     * @return authenticated User entity
     */
    User login(String usernameOrEmail, char[] password);

    /**
     * Changes user password, enforcing policy and uniqueness.
     */
    boolean changePassword(int userId, char[] currentPassword, char[] newPassword, boolean forceChange);

    /**
     * Submits a new user access registration request for administrator review.
     */
    int requestRegistration(AccountRequest request);

    /**
     * Submits a password reset request.
     */
    int requestPasswordReset(String usernameOrEmail, String reason);

    /**
     * Terminates current active session and logs audit event.
     */
    void logout();
}
