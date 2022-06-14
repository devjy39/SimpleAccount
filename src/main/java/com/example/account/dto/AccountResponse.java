package com.example.account.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private String accountNumber;
    private Long balance;
}
