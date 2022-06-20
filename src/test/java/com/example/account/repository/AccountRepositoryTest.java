package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.type.AccountStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class AccountRepositoryTest {

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
        //when
        Account savedAccount = accountRepository.save(account);
        //then
        assertEquals(savedAccount, account);
    }

    @Test
    void findFirstByOrderByIdDesc() {
        //given
        AccountUser testUser = AccountUser.builder().name("testName").build();
        AccountUser savedUser = accountUserRepository.save(testUser);
        Account account = Account.builder().accountUser(savedUser)
                .accountNumber("1111111111")
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .registeredAt(LocalDateTime.now()).build();
        accountRepository.save(account);
        //when
        Optional<Account> optionalAccount = accountRepository.findFirstByOrderByIdDesc();
        //then
        assertEquals(optionalAccount.get().getAccountNumber(),"1111111111");
    }

    @Test
    void findByAccountUserId() {
        //given
        AccountUser testUser = AccountUser.builder().name("testName").build();
        AccountUser savedUser = accountUserRepository.save(testUser);
        Account account = Account.builder().accountUser(savedUser)
                .accountNumber("1111111111")
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .registeredAt(LocalDateTime.now()).build();
        accountRepository.save(account);
        //when
        List<Account> accountList = accountRepository.findByAccountUser(savedUser);
        //then
        assertEquals(accountList.size(), 1);
        assertEquals(accountList.get(0).getAccountUser().getId(), savedUser.getId());
        assertEquals(accountList.get(0).getAccountNumber(),"1111111111");
    }

    @Test
    void countByAccountUserId() {
        //given
        AccountUser testUser = AccountUser.builder().name("testName").build();
        AccountUser savedUser = accountUserRepository.save(testUser);
        Account account = Account.builder().accountUser(savedUser)
                .accountNumber("1111111111")
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .registeredAt(LocalDateTime.now()).build();
        accountRepository.save(account);
        //when
        int count = accountRepository.countByAccountUser(savedUser);

        //then
        assertEquals(count, 1);
    }

    @Test
    void findByAccountNumber() {
        //given
        AccountUser testUser = AccountUser.builder().name("testName").build();
        AccountUser savedUser = accountUserRepository.save(testUser);
        Account account = Account.builder().accountUser(savedUser)
                .accountNumber("1111111111")
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .registeredAt(LocalDateTime.now()).build();
        Account savedAccount = accountRepository.save(account);
        //when
        Optional<Account> optionalAccount = accountRepository.findByAccountNumber(account.getAccountNumber());

        //then
        assertEquals(savedAccount.getAccountNumber(), optionalAccount.get().getAccountNumber());
    }

}