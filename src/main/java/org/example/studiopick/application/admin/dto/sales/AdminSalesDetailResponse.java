package org.example.studiopick.application.admin.dto.sales;

import java.util.List;

public record AdminSalesDetailResponse(
    String startDate,
    String endDate,
    List<AdminSalesDetailData> salesDetails,
    AdminSalesPaginationResponse pagination
) {}