package org.example.studiopick.application.admin.dto.sales;

import java.math.BigDecimal;
import java.util.List;

public record AdminSalesTrendResponse(
    String startDate,
    String endDate,
    String period,
    BigDecimal totalSales,
    List<AdminSalesTrendData> trendData
) {}