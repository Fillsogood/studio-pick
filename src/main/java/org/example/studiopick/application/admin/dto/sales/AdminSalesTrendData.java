package org.example.studiopick.application.admin.dto.sales;

import java.math.BigDecimal;

public record AdminSalesTrendData(
    String period,
    BigDecimal sales
) {}
