package org.example.studiopick.application.admin.dto.sales;

import org.example.studiopick.domain.common.enums.PaymentMethod;

import java.math.BigDecimal;

public record AdminPaymentMethodData(
    PaymentMethod method,
    BigDecimal totalSales,
    Long paymentCount
) {
  public AdminPaymentMethodData(PaymentMethod method, Number totalSales, Long paymentCount) {
    this(method,
        totalSales != null ? new BigDecimal(totalSales.toString()) : BigDecimal.ZERO,
        paymentCount);
  }
}