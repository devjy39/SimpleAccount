package com.example.account.dto;

import com.example.account.type.TransactionResult;
import com.example.account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private String accountNumber;

    private TransactionType transactionType;
    private TransactionResult transactionResult;
    private Long transactionId;
    private Long transactionAmount;

    private LocalDateTime transactedAt;
}
