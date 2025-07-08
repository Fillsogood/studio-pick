package org.example.studiopick.application.payment.dto;

import org.example.studiopick.domain.common.enums.PaymentMethod;
import org.example.studiopick.domain.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentInfoResponse(
    Long id,
    String paymentKey,
    String orderId,
    Long reservationId,
    BigDecimal amount,
    PaymentMethod method,
    PaymentStatus status,
    LocalDateTime paidAt,
    String failureCode,
    String failureReason
) {}