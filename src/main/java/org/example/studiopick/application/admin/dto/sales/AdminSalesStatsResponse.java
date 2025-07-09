package org.example.studiopick.application.admin.dto.sales;

import java.math.BigDecimal;

public record AdminSalesStatsResponse(
    BigDecimal totalSales,
    BigDecimal todaySales,
    BigDecimal thisMonthSales,
    BigDecimal thisYearSales,
    BigDecimal totalRefunds,
    long totalPayments,
    long totalRefundCount
) {}
