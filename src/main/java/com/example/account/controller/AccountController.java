package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountResponse;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/account")
    public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {
        return CreateAccount.Response.from(accountService.createAccount(
                request.getUserId(),
                request.getInitialBalance()));
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(@RequestBody @Valid DeleteAccount.Request request) {
        return DeleteAccount.Response.from(accountService.deleteAccount(
                request.getUserId(),
                request.getAccountNumber()));
    }

    @GetMapping("/account")
    public List<AccountResponse> inquireAccounts(@RequestParam("user_id") @NotNull Long userId) {
        List<Account> accountList = accountService.inquireAccounts(userId);

        return accountList.stream().map(account -> AccountResponse.builder()
                        .accountNumber(account.getAccountNumber())
                        .balance(account.getBalance())
                        .build())
                .collect(Collectors.toList());
    }
}