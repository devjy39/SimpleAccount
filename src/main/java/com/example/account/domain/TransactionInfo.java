package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResult;
import com.example.account.type.TransactionType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.example.account.type.TransactionResult.TRANSACTION_SUCCESS;
import static com.example.account.type.TransactionType.USE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class TransactionInfo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    private TransactionResult transactionResult;

    private String transactionId;
    private Long amount;
    private Long balanceSnapshot;
    private LocalDateTime transactedAt;

    public void transactionResultToCancel() {
        if (transactionType != USE || transactionResult != TRANSACTION_SUCCESS) {
            throw new AccountException(ErrorCode.UNABLE_CANCEL_TRANSACTION);
        }
        transactionResult = TransactionResult.TRANSACTION_CANCEL;
    }
}
