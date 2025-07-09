package org.example.studiopick.application.admin.dto.sales;

import java.math.BigDecimal;
import java.util.List;

public record AdminRefundStatsResponse(
    String startDate,
    String endDate,
    BigDecimal totalRefunds,
    long refundCount,
    double refundRate,
    List<AdminRefundTrendData> refundTrend
) {}