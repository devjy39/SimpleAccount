package com.example.account.dto;

import com.example.account.domain.TransactionInfo;
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

    public static TransactionResponse from(TransactionInfo transactionInfo) {
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
