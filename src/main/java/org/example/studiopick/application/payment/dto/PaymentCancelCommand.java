package org.example.studiopick.application.payment.dto;

import java.math.BigDecimal;

public record PaymentCancelCommand(
    String cancelReason,
    BigDecimal cancelAmount  // 선택적 (부분 취소시)
) {}
