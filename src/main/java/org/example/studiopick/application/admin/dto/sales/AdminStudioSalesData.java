package org.example.studiopick.application.admin.dto.sales;

import org.example.studiopick.domain.common.enums.PaymentMethod;

import java.math.BigDecimal;

public record AdminStudioSalesData(
    Long studioId,
    String studioName,
    BigDecimal totalSales,
    Long paymentCount
) {
  public AdminStudioSalesData(Long studioId,String studioName, Number totalSales, Long paymentCount) {
    this(studioId,
        studioName,
        totalSales != null ? new BigDecimal(totalSales.toString()) : BigDecimal.ZERO,
        paymentCount);
  }
}