package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.TransactionInfo;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionInfoRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResult;
import com.example.account.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TransactionInfoServiceTest {
    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionInfoRepository transactionInfoRepository;

    @InjectMocks
    private TransactionInfoService transactionInfoService;

    @Test
    @DisplayName("거래 사용 사용자가 없는 경우")
    void transactUseFailByUserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactUse("1111111111",
                        123L, 1000L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 사용 소유주 아이디와 불일치한 경우")
    void transactUseFailByUserMismatch() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.ofNullable(AccountUser.builder().id(222L).build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(AccountUser.builder().id(111L).build())
                        .balance(1000L)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactUse("1111111111",
                        100L, 1000L));
        //then
        assertEquals(ErrorCode.ACCOUNT_USER_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 사용 계좌가 해지상태인 경우")
    void transactUseFailByUnregisteredAccount() {
        //given
        AccountUser user = AccountUser.builder().id(123L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.ofNullable(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(1000L)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactUse("1111111111",
                        123L, 1000L));
        //then
        assertEquals(ErrorCode.UNREGISTERED_ACCOUNT, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 사용 잔액이 부족한 경우")
    void transactUseFailByInsufficientBalance() {
        //given
        AccountUser user = AccountUser.builder().id(123L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.ofNullable(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(1000L)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactUse("1111111111",
                        123L, 1500L));
        //then
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 사용 금액이 너무 크거나 작은 경우")
    void transactUseFailByIncorrectAmount() {
        //given
        AccountUser user = AccountUser.builder().id(123L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.ofNullable(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(200000000L)
                        .build()));
        //when
        AccountException smallException = assertThrows(AccountException.class, () ->
                transactionInfoService.transactUse("1111111111",
                        123L, 80L));
        AccountException bigException = assertThrows(AccountException.class, () ->
                transactionInfoService.transactUse("1111111111",
                        123L, 120000000L));
        //then
        assertEquals(ErrorCode.TOO_SMALL_AMOUNT, smallException.getErrorCode());
        assertEquals(ErrorCode.TOO_BIG_AMOUNT, bigException.getErrorCode());
    }

    @Test
    @DisplayName("거래 사용 성공")
    void transactUseSuccess() {
        //given
        AccountUser user = AccountUser.builder().id(123L).build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.ofNullable(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(200000000L)
                        .build()));
        //when
        TransactionDto transactionDto = transactionInfoService.transactUse("1111111111",
                123L, 15000L);
        //then
        assertEquals(TransactionResult.TRANSACTION_SUCCESS, transactionDto.getTransactionResult());
    }

    @Test
    @DisplayName("거래 취소 없는 거래인 경우")
    void transactCancelFailByUserNotFound() {
        //given
        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactCancel("1111111111",
                        "1q2w3e4r5t", 1000L));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 취소할 수 없는 거래건인 경우")
    void transactCancelFailByUnableCancel() {
        //given
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1111111111")
                .build();
        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.ofNullable(TransactionInfo.builder().id(1L)
                        .account(account)
                        .amount(1000L)
                        .transactionType(TransactionType.CANCEL)
                        .transactionResult(TransactionResult.TRANSACTION_FAIL)
                        .transactedAt(LocalDateTime.now())
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder().accountNumber("1111111111")
                        .balance(20000L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactCancel("1111111111",
                        "1q2w3e4r5t", 1000L));
        //then
        assertEquals(ErrorCode.UNABLE_CANCEL_TRANSACTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 금액이 다른 경우")
    void transactCancelFailByMismatchAmount() {
        //given
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1111111111")
                .build();
        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.ofNullable(TransactionInfo.builder().id(1L)
                        .account(account)
                        .amount(5500L)
                        .transactionType(TransactionType.USE)
                        .transactionResult(TransactionResult.TRANSACTION_SUCCESS)
                        .transactedAt(LocalDateTime.now())
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder().accountNumber("1111111111")
                        .balance(20000L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactCancel("1111111111",
                        "1q2w3e4r5t", 1000L));
        //then
        assertEquals(ErrorCode.TRANSACTION_AMOUNT_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 해당 계좌의 거래가 아닌 경우")
    void transactCancelFailByMismatchAccount() {
        //given
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1111111111")
                .build();
        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.ofNullable(TransactionInfo.builder().id(1L)
                        .account(account)
                        .amount(5500L)
                        .transactionType(TransactionType.USE)
                        .transactionResult(TransactionResult.TRANSACTION_SUCCESS)
                        .transactedAt(LocalDateTime.now())
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder().accountNumber("2222222222")
                        .balance(20000L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactCancel("1111111111",
                        "1q2w3e4r5t", 5500L));
        //then
        assertEquals(ErrorCode.ACCOUNT_NUMBER_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 1년 넘은 거래인 경우")
    void transactCancelFailByExceedAYear() {
        //given
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1111111111")
                .build();
        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.ofNullable(TransactionInfo.builder().id(1L)
                        .account(account)
                        .amount(5500L)
                        .transactionType(TransactionType.USE)
                        .transactionResult(TransactionResult.TRANSACTION_SUCCESS)
                        .transactedAt(LocalDateTime.now().minusYears(2))
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder().accountNumber("1111111111")
                        .balance(20000L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.transactCancel("1111111111",
                        "1q2w3e4r5t", 5500L));
        //then
        assertEquals(ErrorCode.EXCEED_DATE_1YEAR, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 성공")
    void transactCancelSuccess() {
        //given
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1111111111")
                .build();
        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.ofNullable(TransactionInfo.builder().id(1L)
                        .account(account)
                        .amount(5500L)
                        .transactionType(TransactionType.USE)
                        .transactionResult(TransactionResult.TRANSACTION_SUCCESS)
                        .transactedAt(LocalDateTime.now())
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(Account.builder().accountNumber("1111111111")
                        .balance(20000L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));
        //when
        TransactionDto transactionDto = transactionInfoService.transactCancel("1111111111",
                "1q2w3e4r5t", 5500L);
        //then
        assertEquals(TransactionResult.TRANSACTION_SUCCESS, transactionDto.getTransactionResult());
    }

    @Test
    @DisplayName("거래 조회 거래 아이디 없는 경우")
    void inquireTransactionFailByNotFoundId() {
        //given
        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class, () ->
                transactionInfoService.inquireTransaction("1q2w3e4r5t"));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    @DisplayName("거래 조회 성공")
    void inquireTransactionSuccess() {
        //given
        AccountUser user = AccountUser.builder().id(123L).build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1111111111")
                .balance(200000000L)
                .build();

        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.ofNullable(TransactionInfo.builder()
                        .account(account)
                        .transactionId("1q2w3e4r5t")
                        .transactionType(TransactionType.USE)
                        .transactionResult(TransactionResult.TRANSACTION_SUCCESS)
                        .amount(10000L)
                        .transactedAt(LocalDateTime.now().minusMonths(1))
                        .build()));
        //when
        TransactionDto transactionDto = transactionInfoService.inquireTransaction("1q2w3e4r5t");
        //then
        assertEquals("1q2w3e4r5t", transactionDto.getTransactionId());
    }
}