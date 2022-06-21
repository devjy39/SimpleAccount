package com.example.account.dto;

import com.example.account.type.ErrorCode;
import lombok.*;

@AllArgsConstructor
@Getter
public class ErrorResponse {
    private ErrorCode errorCode;
    private String errorMessage;
}
