package com.csims;

import com.csims.util.ValidationException;
import com.csims.util.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Task Workflow State Machine Rules Test")
public class TaskWorkflowStateMachineTest {

    private boolean isValidTransition(String currentStatus, String targetStatus, Integer assignedWorkerId) {
        return switch (currentStatus) {
            case "Created" -> targetStatus.equals("Assigned") || (targetStatus.equals("In Progress") && assignedWorkerId != null);
            case "Assigned" -> targetStatus.equals("In Progress") || targetStatus.equals("Created");
            case "In Progress" -> targetStatus.equals("Completed") || targetStatus.equals("Assigned");
            case "Completed" -> targetStatus.equals("In Progress"); // Reopening allowed
            default -> false;
        };
    }

    @Test
    @DisplayName("Valid workflow progression: Created -> Assigned -> In Progress -> Completed")
    void testValidProgression() {
        assertTrue(isValidTransition("Created", "Assigned", 5));
        assertTrue(isValidTransition("Assigned", "In Progress", 5));
        assertTrue(isValidTransition("In Progress", "Completed", 5));
    }

    @Test
    @DisplayName("Invalid workflow jumps: Created -> Completed without progress is rejected")
    void testInvalidJumps() {
        assertFalse(isValidTransition("Created", "Completed", null), "Cannot complete a task directly from Created");
        assertFalse(isValidTransition("Completed", "Created", null), "Cannot jump backwards to Created from Completed");
        assertFalse(isValidTransition("Assigned", "Completed", 5), "Cannot complete without In Progress stage");
    }

    @Test
    @DisplayName("Allow reopening completed task to In Progress")
    void testReopening() {
        assertTrue(isValidTransition("Completed", "In Progress", 5));
    }

    @Test
    @DisplayName("Task completion percentage validation (0 to 100)")
    void testProgressPercentageValidation() {
        assertDoesNotThrow(() -> Validator.validateRange(0, 0, 100, "Progress"));
        assertDoesNotThrow(() -> Validator.validateRange(50, 0, 100, "Progress"));
        assertDoesNotThrow(() -> Validator.validateRange(100, 0, 100, "Progress"));

        assertThrows(ValidationException.class, () -> Validator.validateRange(-1, 0, 100, "Progress"));
        assertThrows(ValidationException.class, () -> Validator.validateRange(101, 0, 100, "Progress"));
    }
}
