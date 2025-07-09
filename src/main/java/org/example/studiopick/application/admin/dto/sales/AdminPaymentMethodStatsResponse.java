package org.example.studiopick.application.admin.dto.sales;

import java.math.BigDecimal;
import java.util.List;

public record AdminPaymentMethodStatsResponse(
    String startDate,
    String endDate,
    BigDecimal totalSales,
    List<AdminPaymentMethodData> methodStats
) {}
