package org.example.studiopick.application.payment.dto;

import lombok.Builder;

@Builder
public record TossPaymentConfirmRequest(
    String paymentKey,
    String orderId,
    Long amount
) {}