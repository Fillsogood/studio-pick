package org.example.studiopick.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record PaymentConfirmResponse(
    String paymentKey,
    String orderId,
    String status,
    BigDecimal amount,
    String method,
    LocalDateTime approvedAt,
    Long reservationId
) {}