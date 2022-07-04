package com.example.account.controller;

import com.example.account.service.AccountLock;
import com.example.account.dto.TransactionCancel;
import com.example.account.dto.TransactionInquiry;
import com.example.account.dto.TransactionUse;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionInfoService transactionInfoService;

    @AccountLock
    @PostMapping("/transaction/use")
    public TransactionUse.Response useAccountBalance(@RequestBody @Valid TransactionUse.Request request) {
        try {
            return TransactionUse.Response.from(transactionInfoService.transactUse(
                    request.getAccountNumber(),
                    request.getUserId(),
                    request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to use Account Balance");

            transactionInfoService.saveFailedTransaction(
                    request.getAccountNumber(),request.getAmount(), USE);

            throw e;
        }
    }

    @AccountLock
    @PostMapping("/transaction/cancel")
    public TransactionCancel.Response cancelUseAccount(@RequestBody @Valid TransactionCancel.Request request) {
        try {
            return TransactionCancel.Response.from(transactionInfoService.transactCancel(
                    request.getAccountNumber(),
                    request.getTransactionId(),
                    request.getAmount()));
        } catch (AccountException e) {
            log.error("Failed to cancel use Account Balance");

            transactionInfoService.saveFailedTransaction(
                    request.getAccountNumber(), request.getAmount(), CANCEL);

            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public TransactionInquiry inquireTransaction(@PathVariable String transactionId) {
        return TransactionInquiry.from(transactionInfoService.inquireTransaction(
                transactionId));
    }
}
