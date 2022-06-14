package com.example.account.controller;

import com.example.account.domain.TransactionInfo;
import com.example.account.dto.TransactionCancel;
import com.example.account.dto.TransactionResponse;
import com.example.account.dto.TransactionUse;
import com.example.account.service.TransactionInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionInfoService transactionInfoService;

    @PostMapping("/transaction/use")
    public TransactionUse.Response useAccountBalance(@RequestBody @Valid TransactionUse.Request request) {
        TransactionInfo transactionInfo = transactionInfoService.transactUse(
                request.getAccountNumber(),
                request.getUserId(),
                request.getTransactionAmount());

        return TransactionUse.Response.builder()
                .accountNumber(transactionInfo.getAccount().getAccountNumber())
                .transactionResult(transactionInfo.getTransactionResult())
                .transactionId(transactionInfo.getId())
                .TransactionAmount(transactionInfo.getAmount())
                .transactedAt(transactionInfo.getTransactedAt())
                .build();
    }

    @PostMapping("/transaction/cancel")
    public TransactionCancel.Response cancelUseAccount(@RequestBody @Valid TransactionCancel.Request request) {
        TransactionInfo transactionInfo = transactionInfoService.transactCancel(
                request.getAccountNumber(),
                request.getTransactionId(),
                request.getCancelAmount());

        return TransactionCancel.Response.builder()
                .accountNumber(transactionInfo.getAccount().getAccountNumber())
                .transactionResult(transactionInfo.getTransactionResult())
                .transactionId(transactionInfo.getId())
                .TransactionAmount(transactionInfo.getAmount())
                .transactedAt(transactionInfo.getTransactedAt())
                .build();
    }

    @GetMapping("/transaction/{transactionId}")
    public TransactionResponse inquireTransaction(@PathVariable Long transactionId) {
        TransactionInfo transactionInfo = transactionInfoService.inquireTransaction(
                transactionId);

        return TransactionResponse.builder()
                .accountNumber(transactionInfo.getAccount().getAccountNumber())
                .transactionType(transactionInfo.getTransactionType())
                .transactionResult(transactionInfo.getTransactionResult())
                .transactionId(transactionInfo.getId())
                .transactionAmount(transactionInfo.getAmount())
                .transactedAt(transactionInfo.getTransactedAt())
                .build();
    }
}
