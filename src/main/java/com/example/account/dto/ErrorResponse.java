package com.example.account.dto;

import com.example.account.type.ErrorCode;
import lombok.*;

@AllArgsConstructor
@Getter // 없으면 response error
public class ErrorResponse {
    private ErrorCode errorCode;
    private String errorMessage;
}
