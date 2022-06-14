package com.example.account.service;

import com.example.account.domain.AccountUser;
import com.example.account.repository.AccountUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountUserServiceTest {
    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountUserService accountUserService;

    @Test
    @DisplayName("유저 생성")
    void createUserTest() {
        //given
        final String name = "testUser";
        given(accountUserRepository.save(any()))
                .willReturn(AccountUser.builder()
                        .name(name)
                        .build());
        //when
        AccountUser accountUser = accountUserService.createAccountUser(name);
        //then
        assertEquals(name, accountUser.getName());
    }

}