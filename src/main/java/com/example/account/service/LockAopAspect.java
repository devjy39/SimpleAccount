package com.example.account.service;

import com.example.account.aop.AccountLockIdInterface;
import com.example.account.service.AccountLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LockAopAspect {
    private final AccountLockService accountLockService;

    @Around("@annotation(AccountLock) && args(request)")
    public Object lockTransaction(ProceedingJoinPoint pjp,
                                  AccountLockIdInterface request) throws Throwable {
        try {
            accountLockService.lock(request.getAccountNumber());
            return pjp.proceed();
        } finally {
            accountLockService.unlock(request.getAccountNumber());
        }
    }

}
