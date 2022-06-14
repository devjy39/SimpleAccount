package com.example.account.config;

import com.example.account.domain.AccountUser;
import com.example.account.service.AccountUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class DataMigration {
    private final AccountUserService accountUserService;

    @PostConstruct
    private void migrateAccountUser() {
        String[] names = {
                "홍길동",
                "강호동",
                "유재석",
                "김제베",
                "호날두",
                "손흥민",
                "이강인",
                "James",
                "testName",
                "kimjava"
        };

        for (String name : names) {
            accountUserService.createAccountUser(name);
        }
    }
}
