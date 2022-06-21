package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(0);
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountNumber("1000000011")
                        .build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .balance(10000L)
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.createAccount(user.getId(), 10000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture()); //파라미터 캡쳐
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(1234L, accountDto.getUserId());
        assertEquals(10000L, accountDto.getBalance());
    }

    @Test
    @DisplayName("첫 계좌 생성 시 계좌번호")
    void createFirstAccount() {
        //given
        AccountUser user = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(0);
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .balance(10000L)
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.createAccount(user.getId(), 10000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
        assertEquals(1234L, accountDto.getUserId());
        assertEquals(10000L, accountDto.getBalance());
    }

    @Test
    @DisplayName("계좌 생성 사용자가 없는 경우")
    void createAccountFailByUserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.createAccount(1234L, 1000L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 생성 실패 계좌 개수 초과")
    void createAccountFailByMaxCount() {
        //given
        AccountUser user = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(user))
                .willReturn(10);
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.createAccount(user.getId(), 1000L));
        //then
        assertEquals(ErrorCode.EXCEED_MAX_ACCOUNT_COUNT, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 삭제 사용자가 없는 경우")
    void deleteAccountFailByUserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, any()));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 삭제 계좌가 없는 경우")
    void deleteAccountFailByAccountNotFound() {
        //given
        AccountUser user = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, "1111111111"));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 삭제 소유주가 다른 경우")
    void deleteAccountFailByAccountMismatch() {
        //given
        AccountUser user = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.ofNullable(Account.builder().accountUser(
                                AccountUser.builder().id(1000L).build())
                        .accountNumber("1111111111")
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, "1111111111"));
        //then
        assertEquals(ErrorCode.ACCOUNT_USER_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 삭제 계좌가 이미 해지된 경우")
    void deleteAccountFailByUnregisteredAccount() {
        //given
        AccountUser user = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111111")
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, "1111111111"));
        //then
        assertEquals(ErrorCode.UNREGISTERED_ACCOUNT, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 삭제 잔액이 남은 경우")
    void deleteAccountFailByRemainedBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111111")
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(550L)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1234L, "1111111111"));
        //then
        assertEquals(ErrorCode.REMAINED_BALANCE, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 삭제 성공")
    void deleteAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .name("홍길동")
                .id(1234L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111111")
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(0L)
                        .registeredAt(LocalDateTime.now())
                        .build()));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.deleteAccount(1234L, "1111111111");
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(1234L, accountDto.getUserId());
        assertEquals("1111111111", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());

    }

    @Test
    @DisplayName("계좌 조회 사용자가 없는 경우")
    void inquireAccountsFailByUserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.inquireAccounts(1234L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 조회 성공")
    void inquireAccountsSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .id(1234L).name("홍길동").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(Arrays.asList(Account.builder().accountUser(user).accountNumber("1111111111").balance(1000L).build(),
                        Account.builder().accountUser(user).accountNumber("1111111112").balance(10000L).build(),
                        Account.builder().accountUser(user).accountNumber("1111111113").balance(100000L).build()));
        //when
        List<AccountDto> accountDtoList = accountService.inquireAccounts(1234L);
        //then
        assertEquals(3, accountDtoList.size());
        assertEquals("1111111111",accountDtoList.get(0).getAccountNumber());
        assertEquals(1000L,accountDtoList.get(0).getBalance());
        assertEquals("1111111112",accountDtoList.get(1).getAccountNumber());
        assertEquals(10000L,accountDtoList.get(1).getBalance());
        assertEquals("1111111113",accountDtoList.get(2).getAccountNumber());
        assertEquals(100000L,accountDtoList.get(2).getBalance());
    }

}