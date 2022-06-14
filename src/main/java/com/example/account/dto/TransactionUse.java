package com.example.account.dto;

import com.example.account.type.TransactionResult;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class TransactionUse {

    @Getter
    @Setter
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;
        @NotNull
        @Size(min = 10, max = 10)
        private String accountNumber;
        @NotNull
        @Min(1)
        private Long TransactionAmount;
    }

    @Setter
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String accountNumber;
        private TransactionResult transactionResult;
        private Long transactionId;
        private Long TransactionAmount;
        private LocalDateTime transactedAt;
    }
}
