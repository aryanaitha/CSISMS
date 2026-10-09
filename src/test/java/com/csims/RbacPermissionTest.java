package com.csims;

import com.csims.model.User;
import com.csims.security.PermissionChecker;
import com.csims.security.SessionContext;
import com.csims.security.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RBAC Permission & SessionContext Tests")
public class RbacPermissionTest {

    @BeforeEach
    @AfterEach
    void cleanSession() {
        SessionContext.getInstance().clear();
    }

    @Test
    @DisplayName("User with specific permissions is allowed; unauthorized actions throw UnauthorizedException")
    void testStandardUserPermissions() {
        User worker = new User();
        worker.setUserId(8);
        worker.setUsername("worker_bill");
        worker.setRoleId(8); // Worker role (not admin)

        SessionContext.getInstance().setSession(worker, Set.of("TASK_VIEW", "ATTENDANCE_VIEW"));

        // Allowed
        assertTrue(PermissionChecker.has("TASK_VIEW"));
        assertTrue(PermissionChecker.has("ATTENDANCE_VIEW"));
        assertDoesNotThrow(() -> PermissionChecker.require("TASK_VIEW"));

        // Disallowed
        assertFalse(PermissionChecker.has("USER_VIEW"));
        assertFalse(PermissionChecker.has("PROJECT_CREATE"));
        assertFalse(PermissionChecker.has("INVENTORY_UPDATE"));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
            PermissionChecker.require("PROJECT_CREATE")
        );
        assertEquals("PROJECT_CREATE", ex.getRequiredPermission());
    }

    @Test
    @DisplayName("Administrator (roleId 1) bypasses all permission checks")
    void testAdminBypass() {
        User admin = new User();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setRoleId(1); // Administrator

        SessionContext.getInstance().setSession(admin, Set.of("USER_VIEW", "USER_CREATE"));

        // Admin has access even to codes not explicitly in the set
        assertTrue(PermissionChecker.has(admin, "ANY_UNDEFINED_PERMISSION"));
        assertDoesNotThrow(() -> PermissionChecker.require(admin, "SUPER_SECRET_ACTION"));
    }

    @Test
    @DisplayName("Unauthenticated session has zero permissions")
    void testUnauthenticatedSession() {
        assertFalse(SessionContext.getInstance().isLoggedIn());
        assertNull(SessionContext.getInstance().getCurrentUser());
        assertFalse(PermissionChecker.has("TASK_VIEW"));

        assertThrows(UnauthorizedException.class, () -> PermissionChecker.require("TASK_VIEW"));
    }
}
