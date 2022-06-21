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
import com.example.account.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.TransactionResult.*;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        assertEquals(ErrorCode.ACCOUNT_USER_UN_MATCH, exception.getErrorCode());
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
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(20000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(account));
        given(transactionInfoRepository.save(any()))
                .willReturn(TransactionInfo.builder()
                        .transactionId("1q2w3e4r5t6y")
                        .account(account)
                        .amount(15000L)
                        .transactionType(USE)
                        .transactionResult(TRANSACTION_SUCCESS)
                        .balanceSnapshot(5000L)
                        .transactedAt(LocalDateTime.now()).build());
        ArgumentCaptor<TransactionInfo> captor = ArgumentCaptor.forClass(TransactionInfo.class);
        //when
        transactionInfoService.transactUse("1111111111",
                123L, 15000L);
        //then
        verify(transactionInfoRepository, times(1)).save(captor.capture());
        assertEquals(USE, captor.getValue().getTransactionType());
        assertEquals(TRANSACTION_SUCCESS, captor.getValue().getTransactionResult());
        assertEquals(5000L,captor.getValue().getBalanceSnapshot());
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
                        .transactionResult(TRANSACTION_FAIL)
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
                        .transactionType(USE)
                        .transactionResult(TRANSACTION_SUCCESS)
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
        assertEquals(ErrorCode.TRANSACTION_AMOUNT_UN_MATCH, exception.getErrorCode());
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
                        .transactionType(USE)
                        .transactionResult(TRANSACTION_SUCCESS)
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
        assertEquals(ErrorCode.ACCOUNT_NUMBER_UN_MATCH, exception.getErrorCode());
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
                        .transactionType(USE)
                        .transactionResult(TRANSACTION_SUCCESS)
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
                .balance(20000L)
                .build();
        given(transactionInfoRepository.findByTransactionId(anyString()))
                .willReturn(Optional.ofNullable(TransactionInfo.builder().id(1L)
                        .account(account)
                        .amount(5500L)
                        .transactionType(USE)
                        .transactionResult(TRANSACTION_SUCCESS)
                        .transactedAt(LocalDateTime.now())
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(account));
        given(transactionInfoRepository.save(any()))
                .willReturn(TransactionInfo.builder()
                        .transactionId("1q2w3e4r5t6y")
                        .account(account)
                        .amount(15000L)
                        .transactionType(CANCEL)
                        .transactionResult(TRANSACTION_CANCEL)
                        .balanceSnapshot(5000L)
                        .transactedAt(LocalDateTime.now()).build());
        ArgumentCaptor<TransactionInfo> captor = ArgumentCaptor.forClass(TransactionInfo.class);
        //when
        transactionInfoService.transactCancel("1111111111",
                "1q2w3e4r5t", 5500L);
        //then
        verify(transactionInfoRepository, times(1)).save(captor.capture());
        assertEquals(TRANSACTION_SUCCESS, captor.getValue().getTransactionResult());
        assertEquals(5500L,captor.getValue().getAmount());
        assertEquals(25500L,captor.getValue().getBalanceSnapshot());
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
                        .transactionType(USE)
                        .transactionResult(TRANSACTION_SUCCESS)
                        .amount(10000L)
                        .transactedAt(LocalDateTime.now().minusMonths(1))
                        .build()));
        //when
        TransactionDto transactionDto = transactionInfoService.inquireTransaction("1q2w3e4r5t");
        //then
        assertEquals("1q2w3e4r5t", transactionDto.getTransactionId());
        assertEquals(10000L,transactionDto.getAmount());
        assertEquals(TRANSACTION_SUCCESS,transactionDto.getTransactionResult());
        assertEquals(USE,transactionDto.getTransactionType());
    }

    @Test
    @DisplayName("실패한 거래 저장")
    void saveFailedTransaction() {
        //given
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1111111111")
                .balance(20000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.ofNullable(account));
        ArgumentCaptor<TransactionInfo> captor = ArgumentCaptor.forClass(TransactionInfo.class);
        //when
        transactionInfoService.saveFailedTransaction("1111111111",
                5000L, USE);
        //then
        verify(transactionInfoRepository, times(1)).save(captor.capture());
        assertEquals(USE, captor.getValue().getTransactionType());
        assertEquals(TRANSACTION_FAIL,captor.getValue().getTransactionResult());
        assertEquals(20000L, captor.getValue().getBalanceSnapshot());
        System.out.println("Test Generated UUID: "+captor.getValue().getTransactionId());
    }
}