package org.example.studiopick.application.admin.dto.sales;

import java.math.BigDecimal;

public record AdminRefundTrendData(
    String date,
    BigDecimal refundAmount,
    long refundCount
) {}