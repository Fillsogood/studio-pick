package org.example.studiopick.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCancelResponse(
    String paymentKey,
    String status,
    BigDecimal cancelAmount,
    String cancelReason,
    LocalDateTime canceledAt
) {}