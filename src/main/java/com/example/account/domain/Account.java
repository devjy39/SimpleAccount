package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountSetting;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor  //builder에 필요
@AllArgsConstructor //builder에 필요
@Builder
@Entity
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AccountUser accountUser;
    private String accountNumber;

    @Enumerated(EnumType.STRING) //enum 값의 실제 문자열을 디비에 저장
    private AccountStatus accountStatus;

    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        if (amount < AccountSetting.MIN_TRANSACTION_AMOUNT.getNumber()) {
            throw new AccountException(ErrorCode.TOO_SMALL_AMOUNT);
        } else if (amount > AccountSetting.MAX_TRANSACTION_AMOUNT.getNumber()) {
            throw new AccountException(ErrorCode.TOO_BIG_AMOUNT);
        }

        balance -= amount;
    }

    public void cancelUseBalance(Long amount) {
        balance += amount;
    }
}
