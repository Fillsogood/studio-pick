package org.example.studiopick.application.payment.dto;

import java.math.BigDecimal;

public record PaymentRequestCommand(
    Long reservationId,
    BigDecimal amount,
    String orderName,
    String customerName,
    String customerEmail
) {}