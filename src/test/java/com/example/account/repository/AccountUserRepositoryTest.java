package com.example.account.repository;

import com.example.account.domain.AccountUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class AccountUserRepositoryTest {
    @Autowired
    private AccountUserRepository accountUserRepository;

    @Test
    void save() {
        //given
        AccountUser user = AccountUser.builder().name("홍길동").build();
        //when
        AccountUser savedUser = accountUserRepository.save(user);
        //then
        assertEquals(savedUser.getId(), 1);
        assertEquals(savedUser.getName(), user.getName());
    }
}