package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class DeleteAccount {
    @Getter
    @Setter
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;
        @NotNull
        @Size(min = 10, max = 10)
        private String accountNumber;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long accountId;
        private String accountNumber;
        private LocalDateTime unRegisteredAt;

        public static Response from(Account account) {
            return Response.builder().accountId(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .unRegisteredAt(account.getUnregisteredAt())
                    .build();
        }
    }
}
