package com.example.account.service;

import com.example.account.domain.AccountUser;
import com.example.account.repository.AccountUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AccountUserService {
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountUser createAccountUser(String name) {
        return accountUserRepository.save(AccountUser.builder()
                .name(name)
                .build());
    }
}
