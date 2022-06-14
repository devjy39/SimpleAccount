package com.example.account.repository;

import com.example.account.domain.TransactionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionInfoRepository extends JpaRepository<TransactionInfo, Long> {
}
