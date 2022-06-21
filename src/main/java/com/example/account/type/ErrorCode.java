package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),
    ARGUMENT_NOT_VALID("입력값이 적절하지 않습니다."),
    USER_NOT_FOUND("사용자가 없습니다."),
    EXCEED_MAX_ACCOUNT_COUNT("계좌 개수가 꽉 찼습니다."),
    ACCOUNT_NOT_FOUND("존재하지 않는 계좌입니다."),
    ACCOUNT_USER_UN_MATCH("계좌의 소유주 정보가 일치하지 않습니다."),
    UNREGISTERED_ACCOUNT("해지된 계좌입니다."),
    REMAINED_BALANCE("계좌에 잔액이 남아있습니다."),
    INSUFFICIENT_BALANCE("계좌에 잔액이 부족합니다."),
    TOO_SMALL_AMOUNT("거래 금액이 너무 작습니다. (100원 이상 가능)"),
    TOO_BIG_AMOUNT("거래 금액이 너무 큽니다. (1억 이하 사용 가능)"),
    TRANSACTION_NOT_FOUND("거래 정보가 없습니다."),
    TRANSACTION_AMOUNT_UN_MATCH("거래금액과 취소금액이 다릅니다."),
    ACCOUNT_NUMBER_UN_MATCH("해당 거래의 계좌번호가 일치하지 않습니다."),
    EXCEED_DATE_1YEAR("거래일이 1년이상 지난 건입니다."),
    UNABLE_CANCEL_TRANSACTION("취소할 수 없는 거래건입니다."),
    CURRENT_UNDER_TRANSACTION("현재 거래중인 계좌입니다. 나중에 다시 시도해주세요.")
    ;

    private final String description; //코드 부가설명
}
