package org.example.studiopick.application.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettlementWithdrawRequestDto {
    private String memo; // 선택: 출금 요청 메모나 사유 등 프론트에서 받는 경우
}