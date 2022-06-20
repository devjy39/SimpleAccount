package com.example.account.dto;

import com.example.account.domain.TransactionInfo;
import com.example.account.type.TransactionResult;
import com.example.account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private String transactionId;
    private String accountNumber;
    private Long amount;

    private TransactionType transactionType;
    private TransactionResult transactionResult;
    private Long balanceSnapshot;

    private LocalDateTime transactedAt;

    public static TransactionDto fromEntity(TransactionInfo transactionInfo) {
        return TransactionDto.builder()
                .transactionId(transactionInfo.getTransactionId())
                .accountNumber(transactionInfo.getAccount().getAccountNumber())
                .amount(transactionInfo.getAmount())
                .transactionType(transactionInfo.getTransactionType())
                .transactionResult(transactionInfo.getTransactionResult())
                .balanceSnapshot(transactionInfo.getBalanceSnapshot())
                .transactedAt(transactionInfo.getTransactedAt())
                .build();
    }
}
