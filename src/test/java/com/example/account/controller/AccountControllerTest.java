package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest //실제 프로그램 구동환경처럼 spring context를 구동하고 bean을 사용할 수 있게됨.
@WebMvcTest(AccountController.class) // 특정 컨트롤러만 격리시켜서 유닛단위로 테스트
public class AccountControllerTest {

    @MockBean // 가짜 빈 등록
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("계좌 생성")
    void createAccount() throws Exception {
        //given
        CreateAccount.Request input = new CreateAccount.Request();
        input.setUserId(10001L);
        input.setInitialBalance(10000L);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        given(accountService.createAccount(any(),any()))
                .willReturn(Account.builder().id(1L).accountNumber("1000000000")
                        .registeredAt(now).build());

        //when
        //then
        mockMvc.perform(post("/account")
                        .content(objectMapper.writeValueAsString(input))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print()) // 상세정보 출력
                .andExpect(status().isOk()) // 값 예상
                .andExpect(jsonPath("$.accountNumber").value("1000000000")) //예상 값
                .andExpect(jsonPath("$.registeredAt").value(now.toString())); //예상 값
    }

    @Test
    @DisplayName("계좌 삭제")
    void deleteAccount() throws Exception {
        //given
        DeleteAccount.Request input = new DeleteAccount.Request();
        input.setUserId(10001L);
        input.setAccountNumber("1111111111");
        LocalDateTime now = LocalDateTime.now().withNano(0);
        given(accountService.deleteAccount(any(),any()))
                .willReturn(Account.builder().id(1L).accountNumber("1111111111")
                        .unregisteredAt(now).build());

        //when
        //then
        mockMvc.perform(delete("/account")
                        .content(objectMapper.writeValueAsString(input))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print()) // 상세정보 출력
                .andExpect(status().isOk()) // 값 예상
                .andExpect(jsonPath("$.accountNumber").value("1111111111"))
                .andExpect(jsonPath("$.unRegisteredAt").value(now.toString()));
    }

    @Test
    @DisplayName("계좌 조회")
    void inquireAccounts() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now().withNano(0);
        given(accountService.inquireAccounts(anyLong()))
                .willReturn(List.of(Account.builder().id(1L).accountNumber("1111111111")
                        .balance(1000L).build(),Account.builder().id(1L).accountNumber("1111111111")
                        .balance(1000L).build(),Account.builder().id(1L).accountNumber("1111111111")
                        .balance(1000L).build()));

        //when
        //then
        mockMvc.perform(get("/account").param("user_id","1"))
                .andDo(print()) // 상세정보 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

    }

}
