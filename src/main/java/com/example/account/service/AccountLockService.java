package com.example.account.service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLockService {
    private final RedissonClient redissonClient;

    public void lock(String accountNumber) {
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying lock for accountNumber :{}", accountNumber);
        try {
            //1초 대기, 5초 후 자동해제
            boolean isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("Lock acquisition failed");
                throw new AccountException(ErrorCode.CURRENT_UNDER_TRANSACTION);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }

    public void unlock(String accountNumber) {
        log.debug("Unlock for accountNumber :{}", accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }
}
