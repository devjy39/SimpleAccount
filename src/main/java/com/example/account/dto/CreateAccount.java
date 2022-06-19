package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateAccount {
    // inner class 로 각 타입 명시
    @Getter
    @Setter
    @ToString
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull
        @Min(100)
        private Long initialBalance;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long accountId;
        private String accountNumber;
        private LocalDateTime registeredAt;

        public static Response from(Account account) {
            return Response.builder()
                    .accountId(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .registeredAt(account.getRegisteredAt())
                    .build();
        }
    }

}
