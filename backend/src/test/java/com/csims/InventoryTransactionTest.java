package com.csims;

import com.csims.exception.ApiException;
import com.csims.model.Material;
import com.csims.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class InventoryTransactionTest {

    @Autowired
    private InventoryService inventoryService;

    @Test
    @DisplayName("Transactional rollback occurs when issue quantity exceeds available stock")
    void issueExceedingStockThrowsExceptionAndRollsBack() {
        // Fetch any existing material
        List<Material> materials = inventoryService.getAllMaterials(null, null);
        assertFalse(materials.isEmpty(), "Materials catalog should not be empty");

        Material target = materials.get(0);
        BigDecimal initialStock = target.getQuantityOnHand();
        BigDecimal excessiveQuantity = initialStock.add(BigDecimal.valueOf(100000));

        // Attempting to issue more than available should throw ApiException
        ApiException exception = assertThrows(ApiException.class, () -> {
            inventoryService.recordIssue(
                    target.getId(),
                    excessiveQuantity,
                    1,
                    "TX-TEST-ROLLBACK",
                    "Automated rollback verification test",
                    1L,
                    "admin"
            );
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));

        // Verify stock quantity is completely unchanged (rollback guaranteed)
        Material afterAttempt = inventoryService.getMaterialById(target.getId());
        assertEquals(0, initialStock.compareTo(afterAttempt.getQuantityOnHand()),
                "Stock quantity must remain unchanged after failed transaction");
    }

    @Test
    @DisplayName("Valid material receipt atomically increases inventory stock")
    void validReceiptIncreasesStock() {
        List<Material> materials = inventoryService.getAllMaterials(null, null);
        assertFalse(materials.isEmpty());

        Material target = materials.get(0);
        BigDecimal initialStock = target.getQuantityOnHand();
        BigDecimal receiptQuantity = BigDecimal.valueOf(25);

        inventoryService.recordReceipt(
                target.getId(),
                receiptQuantity,
                "PO-TEST-001",
                "Automated test delivery",
                1L,
                "admin"
        );

        Material updated = inventoryService.getMaterialById(target.getId());
        assertEquals(0, initialStock.add(receiptQuantity).compareTo(updated.getQuantityOnHand()),
                "Stock quantity should increase by exactly 25 units");
    }
}
