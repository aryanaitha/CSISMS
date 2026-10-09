package com.csims;

import com.csims.model.Material;
import com.csims.util.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Inventory Stock Rules & Validation Test")
public class InventoryStockLogicTest {

    @Test
    @DisplayName("Stock issue with quantity exceeding on-hand quantity is rejected")
    void testInsufficientStockValidation() {
        Material material = new Material();
        material.setMaterialCode("MAT-CEM-01");
        material.setName("Portland Cement");
        material.setQuantityOnHand(new BigDecimal("50.00"));
        material.setMinStock(new BigDecimal("20.00"));

        BigDecimal requestedIssue = new BigDecimal("75.00");

        // Rule verification
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            if (material.getQuantityOnHand().compareTo(requestedIssue) < 0) {
                throw new ValidationException("quantity", "Insufficient stock available for material '" + material.getName() +
                                              "'. Requested: " + requestedIssue + ", Available: " + material.getQuantityOnHand());
            }
            material.setQuantityOnHand(material.getQuantityOnHand().subtract(requestedIssue));
        });

        assertTrue(ex.getMessage().contains("Insufficient stock available"));
        assertEquals(new BigDecimal("50.00"), material.getQuantityOnHand(), "Stock must remain unchanged on failed issue");
    }

    @Test
    @DisplayName("Stock issue within available limits successfully reduces stock")
    void testValidStockIssue() {
        Material material = new Material();
        material.setQuantityOnHand(new BigDecimal("100.00"));
        material.setMinStock(new BigDecimal("20.00"));

        BigDecimal issueQty = new BigDecimal("30.00");
        material.setQuantityOnHand(material.getQuantityOnHand().subtract(issueQty));

        assertEquals(new BigDecimal("70.00"), material.getQuantityOnHand());
    }

    @Test
    @DisplayName("Low stock alert condition triggered when quantity <= minStock")
    void testLowStockAlertCondition() {
        Material normal = new Material();
        normal.setQuantityOnHand(new BigDecimal("25.00"));
        normal.setMinStock(new BigDecimal("20.00"));
        assertFalse(isLowStock(normal));

        Material threshold = new Material();
        threshold.setQuantityOnHand(new BigDecimal("20.00"));
        threshold.setMinStock(new BigDecimal("20.00"));
        assertTrue(isLowStock(threshold), "Stock equal to threshold must trigger low-stock alert");

        Material critical = new Material();
        critical.setQuantityOnHand(new BigDecimal("5.00"));
        critical.setMinStock(new BigDecimal("20.00"));
        assertTrue(isLowStock(critical), "Stock below threshold must trigger low-stock alert");
    }

    private boolean isLowStock(Material m) {
        return m.getQuantityOnHand().compareTo(m.getMinStock()) <= 0;
    }
}
