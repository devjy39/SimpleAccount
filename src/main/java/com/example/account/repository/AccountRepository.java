package com.example.account.repository;

import com.example.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // < entity, pk type >
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByAccountUserId(Long userId);

    Optional<Account> findFirstByOrderByIdDesc();

    int countByAccountUserId(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);
}