package com.example.account.service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountLockServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private AccountLockService accountLockService;

    @Test
    void lockSuccess() throws InterruptedException {
        //given
        given(redissonClient.getLock(any()))
                .willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(true);
        //when
        //then
        assertDoesNotThrow(() -> accountLockService.lock("11111"));
    }

    @Test
    void lockFail() throws InterruptedException {
        //given
        given(redissonClient.getLock(any()))
                .willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(false);
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountLockService.lock("11111"));
        //then
        assertEquals(ErrorCode.CURRENT_UNDER_TRANSACTION, exception.getErrorCode());

    }


}