package com.example.account.controller;

import com.example.account.dto.TransactionCancel;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.TransactionUse;
import com.example.account.service.TransactionInfoService;
import com.example.account.type.TransactionResult;
import com.example.account.type.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    private TransactionInfoService transactionInfoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("잔액 사용")
    void useAccountBalance() throws Exception {
        //given
        TransactionUse.Request input = new TransactionUse.Request();
        input.setAccountNumber("1111111111");
        input.setAmount(10000L);
        input.setUserId(1L);
        LocalDateTime now = LocalDateTime.now().withNano(0);

        given(transactionInfoService.transactUse(anyString(), anyLong(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .transactionId("1q2w3e4r5t")
                        .transactionResult(TransactionResult.TRANSACTION_SUCCESS)
                        .accountNumber("1111111111")
                        .amount(1000L)
                        .transactedAt(now).build());
        //when
        //then
        mockMvc.perform(post("/transaction/use")
                        .content(objectMapper.writeValueAsString(input))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("1q2w3e4r5t"))
                .andExpect(jsonPath("$.accountNumber").value("1111111111"))
                .andExpect(jsonPath("$.transactionResult").value("TRANSACTION_SUCCESS"))
                .andExpect(jsonPath("$.amount").value(1000L))
                .andExpect(jsonPath("$.transactedAt").value(now.toString()));
    }

    @Test
    @DisplayName("잔액 사용 취소")
    void cancelUseAccount() throws Exception {
        //given
        TransactionCancel.Request input = new TransactionCancel.Request();
        input.setAccountNumber("1111111111");
        input.setTransactionId("1q2w3e4r5t");
        input.setAmount(1500L);
        LocalDateTime now = LocalDateTime.now().withNano(0);

        given(transactionInfoService.transactCancel(anyString(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1111111111")
                        .transactionResult(TransactionResult.TRANSACTION_CANCEL)
                        .transactionId("1q2w3e4r5t")
                        .amount(1500L)
                        .transactedAt(now).build());
        //when
        //then
        mockMvc.perform(post("/transaction/cancel")
                        .content(objectMapper.writeValueAsString(input))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("1q2w3e4r5t"))
                .andExpect(jsonPath("$.accountNumber").value("1111111111"))
                .andExpect(jsonPath("$.transactionResult").value("TRANSACTION_CANCEL"))
                .andExpect(jsonPath("$.amount").value(1500L))
                .andExpect(jsonPath("$.transactedAt").value(now.toString()));
    }

    @Test
    @DisplayName("잔액 조회")
    void inquireTransaction() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now().withNano(0);

        given(transactionInfoService.inquireTransaction(anyString()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1111111111")
                        .transactionType(TransactionType.USE)
                        .transactionResult(TransactionResult.TRANSACTION_FAIL)
                        .transactionId("1q2w3e4r5t")
                        .amount(1500L)
                        .transactedAt(now).build());
        //when
        //then
        mockMvc.perform(get("/transaction/1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("1q2w3e4r5t"))
                .andExpect(jsonPath("$.accountNumber").value("1111111111"))
                .andExpect(jsonPath("$.transactionResult").value("TRANSACTION_FAIL"))
                .andExpect(jsonPath("$.transactionType").value("USE"))
                .andExpect(jsonPath("$.amount").value(1500L))
                .andExpect(jsonPath("$.transactedAt").value(now.toString()));
    }


}