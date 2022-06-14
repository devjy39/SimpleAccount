package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 사용자가 없는 경우")
    void createAccountFailByUserNotFound() {
        //given
        given(accountUserRepository.findById(1234L))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.createAccount(1234L, 1000L));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("계좌 생성 실패 계좌 개수 초과")
    void createAccountFailByMaxCount() {
        //given
        AccountUser testUser = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        given(accountRepository.countByAccountUserId(testUser.getId()))
                .willReturn(10);
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.createAccount(testUser.getId(), 1000L));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.EXCEED_MAX_ACCOUNT_COUNT);
    }

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccountSuccess() {
        //given
        AccountUser testUser = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        given(accountRepository.countByAccountUserId(testUser.getId()))
                .willReturn(0);
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountNumber("1000000001")
                        .build()));
        //when
        Account account = accountService.createAccount(testUser.getId(), 10000L);

        System.out.println(account.toString());
        //then
        assertEquals(account.getAccountNumber(), "1000000002");
        assertEquals(account.getAccountUser(), testUser);
        assertEquals(account.getBalance(), 10000L);
        assertEquals(account.getAccountStatus(), AccountStatus.IN_USE);
    }

    @Test
    @DisplayName("계좌 삭제 사용자가 없는 경우")
    void deleteAccountFailByUserNotFound() {
        //given
        given(accountUserRepository.findById(1234L))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, any()));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("계좌 삭제 계좌가 없는 경우")
    void deleteAccountFailByAccountNotFound() {
        //given
        AccountUser testUser = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, "1111111111"));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("계좌 삭제 소유주가 다른 경우")
    void deleteAccountFailByAccountMismatch() {
        //given
        AccountUser testUser = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.ofNullable(Account.builder().accountUser(
                                AccountUser.builder().id(1000L).build())
                        .accountNumber("1111111111")
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, "1111111111"));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.ACCOUNT_USER_MISMATCH);
    }

    @Test
    @DisplayName("계좌 삭제 계좌가 이미 해지된 경우")
    void deleteAccountFailByUnregisteredAccount() {
        //given
        AccountUser testUser = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(testUser)
                        .accountNumber("1111111111")
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, "1111111111"));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.UNREGISTERED_ACCOUNT);
    }

    @Test
    @DisplayName("계좌 삭제 잔액이 남은 경우")
    void deleteAccountFailByRemainedBalance() {
        //given
        AccountUser testUser = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(testUser)
                        .accountNumber("1111111111")
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(550L)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, "1111111111"));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.REMAINED_BALANCE);
    }

    @Test
    @DisplayName("계좌 삭제 성공")
    void deleteAccountSuccess() {
        //given
        AccountUser testUser = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        Account account = Account.builder()
                .accountUser(testUser)
                .accountNumber("1111111111")
                .accountStatus(AccountStatus.IN_USE)
                .balance(0L)
                .registeredAt(LocalDateTime.now())
                .build();
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.ofNullable(account));
        //when
        Account deletedAccount = accountService.deleteAccount(1234L, "1111111111");
        //then
        assertEquals(deletedAccount, account);
    }

    @Test
    @DisplayName("계좌 조회 사용자가 없는 경우")
    void inquireAccountsFailByUserNotFound() {
        //given
        given(accountUserRepository.findById(1234L))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.inquireAccounts(1234L));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("계좌 조회 성공")
    void inquireAccountsSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1234L).name("홍길동").build();
        given(accountUserRepository.findById(1234L))
                .willReturn(Optional.ofNullable(user));
        List<Account> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Account account = Account.builder().accountUser(user).build();
            account.setAccountNumber("111111111" + i);
            account.setBalance(1000L * i);
            account.setAccountStatus(AccountStatus.IN_USE);
            account.setId(i + 1L);
            account.setCreatedAt(LocalDateTime.now());
            list.add(account);
        }
        given(accountRepository.findByAccountUserId(1234L))
                .willReturn(list);
        //when
        List<Account> accountList = accountService.inquireAccounts(1234L);
        //then
        assertEquals(accountList.size(), 6);
    }

}