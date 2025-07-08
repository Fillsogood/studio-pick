package org.example.studiopick.application.payment.dto;

import java.math.BigDecimal;

public record PaymentConfirmCommand(
    String paymentKey,
    String orderId,
    BigDecimal amount
) {}
