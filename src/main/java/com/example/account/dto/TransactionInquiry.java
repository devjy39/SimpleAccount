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
public class TransactionInquiry {
    private String accountNumber;

    private TransactionType transactionType;
    private TransactionResult transactionResult;
    private String transactionId;
    private Long amount;

    private LocalDateTime transactedAt;

    public static TransactionInquiry from(TransactionDto transactionDto) {
        return TransactionInquiry.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionType(transactionDto.getTransactionType())
                .transactionResult(transactionDto.getTransactionResult())
                .transactionId(transactionDto.getTransactionId())
                .amount(transactionDto.getAmount())
                .transactedAt(transactionDto.getTransactedAt())
                .build();
    }
}
