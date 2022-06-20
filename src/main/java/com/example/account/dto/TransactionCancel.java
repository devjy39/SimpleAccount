package com.example.account.dto;

import com.example.account.type.TransactionResult;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class TransactionCancel {

    @Getter
    @Setter
    public static class Request {
        @NotNull
        private String transactionId;
        @NotNull
        @Size(min = 10, max = 10)
        private String accountNumber;
        @NotNull
        @Min(1)
        private Long amount;
    }

    @Setter
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String accountNumber;
        private TransactionResult transactionResult;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactedAt;

        public static Response from(TransactionDto transactionDto) {
            return TransactionCancel.Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionResult(transactionDto.getTransactionResult())
                    .transactionId(transactionDto.getTransactionId())
                    .amount(transactionDto.getAmount())
                    .transactedAt(transactionDto.getTransactedAt())
                    .build();
        }
    }
}
