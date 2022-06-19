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
        return TransactionUse.Response.from(transactionInfoService.transactUse(
                request.getAccountNumber(),
                request.getUserId(),
                request.getTransactionAmount()));
    }

    @PostMapping("/transaction/cancel")
    public TransactionCancel.Response cancelUseAccount(@RequestBody @Valid TransactionCancel.Request request) {
        return TransactionCancel.Response.from(transactionInfoService.transactCancel(
                request.getAccountNumber(),
                request.getTransactionId(),
                request.getCancelAmount()));
    }

    @GetMapping("/transaction/{transactionId}")
    public TransactionResponse inquireTransaction(@PathVariable Long transactionId) {
        return TransactionResponse.from(transactionInfoService.inquireTransaction(
                transactionId));
    }
}
