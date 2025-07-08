package org.example.studiopick.application.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record TossPaymentCancelResponse(
    String paymentKey,
    String orderId,
    String orderName,
    String status,
    LocalDateTime requestedAt,
    LocalDateTime approvedAt,
    Long totalAmount,
    Long balanceAmount,
    Long suppliedAmount,
    Long vat,
    String cultureExpense,
    Long taxFreeAmount,
    Integer taxExemptionAmount,

    @JsonProperty("cancels")
    List<TossCancelInfo> cancels,

    String method,
    String version
) {

  public record TossCancelInfo(
      Long cancelAmount,
      String cancelReason,
      Long taxFreeAmount,
      Integer taxExemptionAmount,
      Long refundableAmount,
      LocalDateTime canceledAt,
      String transactionKey
  ) {}
}