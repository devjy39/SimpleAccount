package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.TransactionInfo;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionInfoRepository;
import com.example.account.type.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.account.type.TransactionResult.TRANSACTION_FAIL;
import static com.example.account.type.TransactionResult.TRANSACTION_SUCCESS;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;

@Service
@RequiredArgsConstructor
public class TransactionInfoService {
    private final TransactionInfoRepository transactionInfoRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    /**
     * 사용자가 없는 경우
     * 소유주 아이디와 불일치
     * 계좌가 해지상태인 경우
     * 잔액이 부족할 경우
     * 거래금액이 너무 크거나 작은 경우
     */
    @Transactional(dontRollbackOn = AccountException.class)
    public TransactionDto transactUse(String accountNumber, Long userId, Long amount) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateTransactUse(accountUser, account);

        account.useBalance(amount); //엔티티 수정 로직을 엔티티 메서드로

        return TransactionDto.fromEntity(
                saveTransaction(account, amount, USE, TRANSACTION_SUCCESS));
    }

    private void validateTransactUse(AccountUser accountUser, Account account) {
        if (accountUser != account.getAccountUser()) {
            throw new AccountException(ErrorCode.ACCOUNT_USER_MISMATCH);
        }

        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ErrorCode.UNREGISTERED_ACCOUNT);
        }
    }

    /**
     * 해당 거래 아이디가 없는 경우
     * 취소할 수 없는 거래건인 경우
     * 거래 금액과 취소 금액이 다른 경우
     * 해당 계좌의 거래가 아닌경우
     * 1년이 넘은 거래 건인 경우
     */
    @Transactional(dontRollbackOn = AccountException.class)
    public TransactionDto transactCancel(String accountNumber, String transactionId, Long cancelAmount) {
        TransactionInfo transactionInfo = transactionInfoRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateCancelUseBalance(cancelAmount, transactionInfo, account);

        account.cancelUseBalance(cancelAmount);
        transactionInfo.transactionResultToCancel();

        return TransactionDto.fromEntity(saveTransaction(
                account, cancelAmount, CANCEL, TRANSACTION_SUCCESS));
    }

    private void validateCancelUseBalance(Long cancelAmount, TransactionInfo transactionInfo, Account account) {
        if (!cancelAmount.equals(transactionInfo.getAmount())) {
            throw new AccountException(ErrorCode.TRANSACTION_AMOUNT_MISMATCH);
        }

        if (!account.getAccountNumber().equals(transactionInfo.getAccount().getAccountNumber())) {
            throw new AccountException(ErrorCode.ACCOUNT_NUMBER_MISMATCH);
        }

        if (transactionInfo.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))) {
            throw new AccountException(ErrorCode.EXCEED_DATE_1YEAR);
        }
    }

    /**
     * 해당 거래 아이디가 없는 경우
     * 실패한 거래도 확인 가능해야 함
     */
    @Transactional
    public TransactionDto inquireTransaction(String transactionId) {
        return TransactionDto.fromEntity(transactionInfoRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND)));
    }

    @Transactional
    public void saveFailedTransaction(String accountNumber, Long amount, TransactionType type) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveTransaction(account, amount, type, TRANSACTION_FAIL);
    }

    private TransactionInfo saveTransaction(Account account, Long amount,
                                            TransactionType type, TransactionResult result) {
        TransactionInfo transactionInfo = TransactionInfo.builder()
                .account(account)
                .transactionType(type)
                .transactionResult(result)
                .amount(amount)
                .balanceSnapshot(account.getBalance())
                .transactionId(UUID.randomUUID().toString().replace("-", ""))
                .transactedAt(LocalDateTime.now())
                .build();
        transactionInfoRepository.save(transactionInfo);
        return transactionInfo;
    }
}
