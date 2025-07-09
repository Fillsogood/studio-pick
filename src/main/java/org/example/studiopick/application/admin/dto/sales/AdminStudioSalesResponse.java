package org.example.studiopick.application.admin.dto.sales;

import java.util.List;

public record AdminStudioSalesResponse(
    String startDate,
    String endDate,
    List<AdminStudioSalesData> studioSales,
    AdminSalesPaginationResponse pagination
) {}
