package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountSetting {
    MAX_ACCOUNT_COUNT(10),
    INITIAL_ACCOUNT_NUMBER(1000000000),
    MIN_TRANSACTION_AMOUNT(100),
    MAX_TRANSACTION_AMOUNT(100_000_000);

    private final int number;
}
