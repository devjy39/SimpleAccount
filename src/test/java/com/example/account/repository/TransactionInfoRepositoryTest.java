package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.TransactionInfo;
import com.example.account.type.AccountStatus;
import com.example.account.type.TransactionResult;
import com.example.account.type.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class TransactionInfoRepositoryTest {

    @Autowired
    private TransactionInfoRepository transactionInfoRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Test
    void save() {
        //given
        AccountUser testUser = AccountUser.builder().name("testName").build();
        AccountUser savedUser = accountUserRepository.save(testUser);
        Account account = Account.builder().accountUser(savedUser)
                .accountNumber("1111111111")
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .registeredAt(LocalDateTime.now()).build();
        Account savedAccount = accountRepository.save(account);

        TransactionInfo transactionInfo = TransactionInfo.builder()
                .account(account)
                .transactionType(TransactionType.USE)
                .transactionResult(TransactionResult.TRANSACTION_SUCCESS)
                .amount(10000L)
                .balanceSnapshot(account.getBalance())
                .transactedAt(LocalDateTime.now())
                .build();
        //when
        TransactionInfo savedTransactionInfo = transactionInfoRepository.save(transactionInfo);

        //then

        assertEquals(savedTransactionInfo.getId(), 1);
        assertEquals(savedTransactionInfo.getTransactionType(),TransactionType.USE);
        assertEquals(savedTransactionInfo.getTransactionResult(),TransactionResult.TRANSACTION_SUCCESS);
        assertEquals(savedTransactionInfo.getAmount(),10000L);
        assertEquals(savedTransactionInfo.getBalanceSnapshot(), account.getBalance());
        assertEquals(savedTransactionInfo.getTransactedAt(), transactionInfo.getTransactedAt());
    }

}