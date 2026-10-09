package com.csims.dao;

import com.csims.model.AccountRequest;
import java.util.List;
import java.util.Optional;

public interface AccountRequestDAO {
    int create(AccountRequest request);
    Optional<AccountRequest> findById(int requestId);
    List<AccountRequest> findAll();
    List<AccountRequest> findByStatus(String status);
    List<AccountRequest> search(String keyword, String type, String status);
    boolean updateReview(int requestId, String status, int reviewerId, String reviewNote);
}
