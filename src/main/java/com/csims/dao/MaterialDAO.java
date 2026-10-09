package com.csims.dao;

import com.csims.model.Material;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MaterialDAO {
    Optional<Material> findById(int materialId);
    Optional<Material> findByCode(String materialCode);
    List<Material> findAll();
    List<Material> search(String keyword, String category, Boolean lowStockOnly);
    List<Material> findLowStock();
    int create(Material material);
    boolean update(Material material);
    boolean updateQuantity(int materialId, BigDecimal newQuantity);
    boolean updateQuantityInTransaction(Connection conn, int materialId, BigDecimal newQuantity) throws SQLException;
    boolean delete(int materialId);
    int countTotal();
    int countLowStock();
}
