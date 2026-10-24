package com.csims.repository;

import com.csims.model.AccountRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRequestRepository {
    private final UserRepository userRepository;

    public AccountRequestRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long create(AccountRequest req) {
        return userRepository.createAccountRequest(req);
    }

    public Optional<AccountRequest> findById(Long id) {
        return userRepository.findAccountRequestById(id);
    }

    public List<AccountRequest> findAll(String status) {
        return userRepository.findAllAccountRequests(status);
    }

    public void updateReview(Long id, String status, Long reviewerId, String notes) {
        userRepository.updateAccountRequestReview(id, status, reviewerId, notes);
    }
}
