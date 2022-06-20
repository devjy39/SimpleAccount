package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {
    private Long userId;
    private String accountNumber;
    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .registeredAt(account.getRegisteredAt())
                .unRegisteredAt(account.getUnRegisteredAt())
                .build();
    }
}
/*
*   Entity의 값이 변하면 Repository 클래스의 Entity Manager의 flush가 호출될 때
*   DB에 값이 반영되고, 이는 다른 로직들에도 영향을 미친다.
*   때문에 View와 통신하면서 필연적으로 데이터의 변경이 많은 DTO 클래스를 분리해주어야 한다.
*   fromEntity 메서드는 변환 시 보일러플레이트 코드를 줄여줄 수 있다.
* */