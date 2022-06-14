package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.TransactionInfo;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionInfoRepository;
import com.example.account.type.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

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
    public TransactionInfo transactUse(String accountNumber, Long userId, Long transactionAmount) {
        Optional<AccountUser> accountUser = accountUserRepository.findById(userId);
        if (accountUser.isEmpty()) {
            return saveUseTransaction(null, transactionAmount,
                    ErrorCode.USER_NOT_FOUND);
        }

        Optional<Account> OptAccount = accountRepository.findByAccountNumber(accountNumber);
        if (OptAccount.isEmpty()) {
            return saveUseTransaction(null, transactionAmount,
                    ErrorCode.ACCOUNT_NOT_FOUND);
        }
        Account account = OptAccount.get();

        if (!userId.equals(account.getAccountUser().getId())) {
            return saveUseTransaction(account,transactionAmount,
                    ErrorCode.ACCOUNT_USER_MISMATCH);
        }

        if (AccountStatus.UNREGISTERED.equals(account.getAccountStatus())) {
            return saveUseTransaction(account,transactionAmount,
                    ErrorCode.UNREGISTERED_ACCOUNT);
        }

        if (account.getBalance() < transactionAmount) {
            return saveUseTransaction(account,transactionAmount,
                    ErrorCode.INSUFFICIENT_BALANCE);
        } else if (transactionAmount < AccountSetting.MIN_TRANSACTION_AMOUNT.getNumber()) {
            return saveUseTransaction(account,transactionAmount,
                    ErrorCode.TOO_SMALL_AMOUNT);
        } else if (transactionAmount > AccountSetting.MAX_TRANSACTION_AMOUNT.getNumber()) {
            return saveUseTransaction(account,transactionAmount,
                    ErrorCode.TOO_BIG_AMOUNT);
        }

        //동시성

        return saveUseTransaction(account, transactionAmount, null);
    }

    private TransactionInfo saveUseTransaction(Account account, Long transactionAmount,
                                               ErrorCode errorCode) {
        boolean isFail = (errorCode != null);

        TransactionInfo transactionInfo = TransactionInfo.builder()
                .account(account)
                .transactionType(TransactionType.USE)
                .transactionResult(isFail ? TransactionResult.TRANSACTION_FAIL :
                        TransactionResult.TRANSACTION_SUCCESS)
                .amount(transactionAmount)
                .balanceSnapshot(account == null ? 0 : account.getBalance())
                .transactedAt(LocalDateTime.now())
                .build();

        transactionInfoRepository.save(transactionInfo);

        if (isFail) {
            throw new AccountException(errorCode);
        }

        updateBalance(account, -transactionAmount);

        return transactionInfo;
    }

    private void updateBalance(Account account, Long transactionAmount) {
        if (account == null) {
            return;
        }
        account.setBalance(account.getBalance() + transactionAmount);
        accountRepository.save(account);
    }

    /**
     * 해당 거래 아이디가 없는 경우
     * 취소할 수 없는 거래건인 경우
     * 거래 금액과 취소 금액이 다른 경우
     * 해당 계좌의 거래가 아닌경우
     * 1년이 넘은 거래 건인 경우
     */
    @Transactional(dontRollbackOn = AccountException.class)
    public TransactionInfo transactCancel(String accountNumber, Long transactionId, Long cancelAmount) {
        Optional<TransactionInfo> optTransactionInfo = transactionInfoRepository.findById(transactionId);
        if (optTransactionInfo.isEmpty()) {
            return saveCancelTransaction(cancelAmount, null,
                    ErrorCode.TRANSACTION_NOT_FOUND);
        }
        TransactionInfo transactionInfo = optTransactionInfo.get();

        if (transactionInfo.getTransactionType() != TransactionType.USE ||
            transactionInfo.getTransactionResult() != TransactionResult.TRANSACTION_SUCCESS) {
            return saveCancelTransaction(cancelAmount, transactionInfo,
                    ErrorCode.UNABLE_CANCEL_TRANSACTION);
        }

        if (!cancelAmount.equals(transactionInfo.getAmount())) {
            return saveCancelTransaction(cancelAmount,
                    transactionInfo, ErrorCode.TRANSACTION_AMOUNT_MISMATCH);
        }

        if (!accountNumber.equals(transactionInfo.getAccount().getAccountNumber())) {
            return saveCancelTransaction(cancelAmount,
                    transactionInfo, ErrorCode.ACCOUNT_NUMBER_MISMATCH);
        }

        if (transactionInfo.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))) {
            return saveCancelTransaction(cancelAmount,
                    transactionInfo, ErrorCode.EXCEED_DATE_1YEAR);
        }

        return saveCancelTransaction(cancelAmount, transactionInfo, null);
    }

    private TransactionInfo saveCancelTransaction(Long cancelAmount,
                                                  TransactionInfo transactionInfo,
                                                  ErrorCode errorCode) {
        boolean isFail = (errorCode != null);

        TransactionInfo newTransaction = TransactionInfo.builder()
                .account(transactionInfo == null ? null : transactionInfo.getAccount())
                .transactionType(TransactionType.CANCEL)
                .transactionResult(isFail ? TransactionResult.TRANSACTION_FAIL :
                        TransactionResult.TRANSACTION_SUCCESS)
                .amount(cancelAmount)
                .balanceSnapshot(transactionInfo == null ? 0L : transactionInfo.getAccount().getBalance())
                .transactedAt(LocalDateTime.now())
                .build();

        transactionInfoRepository.save(newTransaction);

        if (isFail) {
            throw new AccountException(errorCode);
        }

        //사용 취소
        updateTransactionResult(transactionInfo);
        updateBalance(newTransaction.getAccount(), cancelAmount);

        return transactionInfo;
    }

    private void updateTransactionResult(TransactionInfo transactionInfo) {
        if (transactionInfo == null) {
            return;
        }
        transactionInfo.setTransactionResult(TransactionResult.TRANSACTION_CANCEL);
        transactionInfoRepository.save(transactionInfo);
    }

    /**
     *  해당 거래 아이디가 없는 경우
     *  실패한 거래도 확인 가능해야 함
     */
    @Transactional
    public TransactionInfo inquireTransaction(Long transactionId) {
        return transactionInfoRepository.findById(transactionId)
                .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));
    }
}
