package org.example.studiopick.application.payment.dto;

import java.math.BigDecimal;

public record PaymentRequestResponse(
    String orderId,
    BigDecimal amount,
    String orderName,
    String customerName,
    String clientKey  // 프론트엔드에서 토스 결제창 호출용
) {}