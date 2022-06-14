package com.example.account.aop;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Slf4j
@RequiredArgsConstructor
@Aspect
public class ConcurrencyTransactAspect {
    private final RedissonClient redissonClient;

    @Pointcut("execution(public * com.example.account.service.TransactionInfoService.transactUse(..))")
    public void transactUseAspect() {}

    @Pointcut("execution(public * com.example.account.service.TransactionInfoService.transactCancel(..))")
    public void transactCancelAspect() {}

    @Around("(transactUseAspect() || transactCancelAspect()) && args(accountNumber,..)")
    public Object lockTransaction(ProceedingJoinPoint pjp, String accountNumber) throws Throwable {
        accountLock(accountNumber);
        log.info("계좌번호 :"+accountNumber+" 거래 시작 Locking Start");

        Object proceed = pjp.proceed();

        accountUnlock(accountNumber);
        log.info("계좌번호 :" + accountNumber + " 거래 종료 Locking End");

        return proceed;
    }

    public void accountLock(String accountNumber) {
        RLock lock = redissonClient.getLock(accountNumber);

        if (lock.isLocked()) {
            log.error("========Lock acquisition failed ===========");
            throw new AccountException(ErrorCode.CURRENT_UNDER_TRANSACTION);
        }

        log.info("lock success! locking :"+lock.tryLock());
    }

    public void accountUnlock(String accountNumber) {
        RLock lock = redissonClient.getLock(accountNumber);
        if (lock.isLocked()) {
            lock.unlock();
        }
        log.info("unlock success!!!!!");
    }

}
