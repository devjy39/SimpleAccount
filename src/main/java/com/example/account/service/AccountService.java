package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import com.example.account.type.AccountSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor // 제약사항이나 final field 생성자 생성
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     *  사용자 있는지 조회
     *  계좌가 10개인 경우 조회
     *  계좌 번호 생성
     *  계좌 저장 후 정보 반환
     */
    @Transactional
    public Account createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = isExistUser(userId);

        int count = accountRepository.countByAccountUserId(userId);

        if (count >= AccountSetting.MAX_ACCOUNT_COUNT.getNumber()) {
            throw new AccountException(ErrorCode.EXCEED_MAX_ACCOUNT_COUNT);
        }

        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> Long.parseLong(account.getAccountNumber()) + 1 + "")
                .orElse(String.valueOf(AccountSetting.INITIAL_ACCOUNT_NUMBER.getNumber()));

        Account newAccount = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber(newAccountNumber)
                .balance(initialBalance)
                .registeredAt(LocalDateTime.now())
                .build();

        accountRepository.save(newAccount);
        return newAccount;
    }

    /**
     *  사용자가 없는경우
     *  계좌가 없는경우
     *  소유주가 다른경우
     *  계좌가 이미 해지된 경우
     *  잔액이 남은경우 실패
     *  해지 후 정보 반환
     */
    @Transactional
    public Account deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = isExistUser(userId);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!accountUser.getId().equals(account.getAccountUser().getId())) {
            throw new AccountException(ErrorCode.ACCOUNT_USER_MISMATCH);
        }

        if (account.getAccountStatus().equals(AccountStatus.UNREGISTERED)) {
            throw new AccountException(ErrorCode.UNREGISTERED_ACCOUNT);
        }

        if (account.getBalance() > 0) {
            throw new AccountException(ErrorCode.REMAINED_BALANCE);
        }

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        accountRepository.save(account);

        return account;
    }

    /**
     *  사용자가 없는 경우 실패
     *  userId와 일치하는 계좌 조회
     *  List<Account>로 응답
     */
    @Transactional
    public List<Account> inquireAccounts(Long userId) {
        AccountUser accountUser = isExistUser(userId);

        return accountRepository.findByAccountUserId(accountUser.getId());
    }

    private AccountUser isExistUser(Long userId) {
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
    }
}
