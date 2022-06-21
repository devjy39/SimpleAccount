package com.example.account.aop;

import com.example.account.dto.TransactionUse;
import com.example.account.exception.AccountException;
import com.example.account.service.AccountLockService;
import com.example.account.type.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {
    @Mock
    private AccountLockService accountLockService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @InjectMocks
    private LockAopAspect lockAopAspect;

    @Test
    void accountLockAndUnlock() throws Throwable {
        //given
        ArgumentCaptor<String> lockCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockCaptor = ArgumentCaptor.forClass(String.class);

        TransactionUse.Request request = new TransactionUse.Request(111L, "1111111111", 500L);
        //when
        lockAopAspect.lockTransaction(proceedingJoinPoint, request);
        //then
        verify(accountLockService,times(1)).lock(lockCaptor.capture());
        verify(accountLockService,times(1)).unlock(unlockCaptor.capture());
        assertEquals(request.getAccountNumber(),lockCaptor.getValue());
        assertEquals(request.getAccountNumber(),unlockCaptor.getValue());
    }

    @Test
    void accountLockAndUnlockIfThrow() throws Throwable {
        //given
        ArgumentCaptor<String> lockCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockCaptor = ArgumentCaptor.forClass(String.class);

        TransactionUse.Request request = new TransactionUse.Request(111L, "1111111111", 500L);
        given(proceedingJoinPoint.proceed())
                .willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
        //when
        assertThrows(AccountException.class,
                () -> lockAopAspect.lockTransaction(proceedingJoinPoint, request));

        //then
        verify(accountLockService,times(1)).lock(lockCaptor.capture());
        verify(accountLockService,times(1)).unlock(unlockCaptor.capture());
        assertEquals(request.getAccountNumber(),lockCaptor.getValue());
        assertEquals(request.getAccountNumber(),unlockCaptor.getValue());
    }
}