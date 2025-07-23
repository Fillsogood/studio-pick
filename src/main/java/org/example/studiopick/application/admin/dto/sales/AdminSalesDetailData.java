package org.example.studiopick.application.admin.dto.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record AdminSalesDetailData(
    Long paymentId,
    String orderId,
    String userName,
    String studioName,
    BigDecimal amount,
    String paymentMethod,
    String status,
    LocalDateTime paidAt,
    LocalDateTime createdAt
) {}