package org.example.studiopick.application.payment.dto;

import lombok.Builder;

@Builder
public record TossPaymentCancelRequest(
    String cancelReason,
    Long cancelAmount    // 부분 취소시 사용 (선택적)
//    Long refundableAmount, // 환불 가능 금액 (선택적)
//    Long taxFreeAmount     // 비과세 금액 (선택적)
) {}