package com.csims.dao;

import com.csims.model.InventoryTransaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryTransactionDAO {
    Optional<InventoryTransaction> findById(int transactionId);
    List<InventoryTransaction> findAll();
    List<InventoryTransaction> search(Integer materialId, Integer projectId, String type, LocalDate startDate, LocalDate endDate);
    int create(InventoryTransaction tx);
    int createInTransaction(Connection conn, InventoryTransaction tx) throws SQLException;
}
