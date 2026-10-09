package com.csims.dao;

import com.csims.model.Contractor;
import java.util.List;
import java.util.Optional;

public interface ContractorDAO {
    Optional<Contractor> findById(int contractorId);
    List<Contractor> findAll();
    List<Contractor> search(String keyword);
    int create(Contractor contractor);
    boolean update(Contractor contractor);
    boolean delete(int contractorId);
}
