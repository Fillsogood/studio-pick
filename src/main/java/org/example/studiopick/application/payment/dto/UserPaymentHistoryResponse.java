package org.example.studiopick.application.payment.dto;

import org.example.studiopick.domain.common.enums.PaymentMethod;
import org.example.studiopick.domain.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record UserPaymentHistoryResponse(
    Long paymentId,
    String orderId,
    String paymentKey,
    Long reservationId,
    String studioName,
    String reservationDate,
    String reservationTime,
    BigDecimal amount,
    PaymentMethod method,
    PaymentStatus status,
    LocalDateTime paidAt,
    String failureReason
) {}
