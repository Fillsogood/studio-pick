package org.example.studiopick.application.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)  // ✅ 알 수 없는 필드 무시
public record TossPaymentResponse(
    @JsonProperty("paymentKey") String paymentKey,
    @JsonProperty("orderId") String orderId,
    @JsonProperty("orderName") String orderName,
    @JsonProperty("status") String status,
    @JsonProperty("requestedAt") OffsetDateTime requestedAt,
    @JsonProperty("approvedAt") OffsetDateTime approvedAt,
    @JsonProperty("totalAmount") Long totalAmount,
    @JsonProperty("balanceAmount") Long balanceAmount,
    @JsonProperty("method") String method,
    @JsonProperty("card") TossCardInfo card,
    @JsonProperty("virtualAccount") TossVirtualAccountInfo virtualAccount,
    @JsonProperty("transfer") TossTransferInfo transfer,
    @JsonProperty("mobilePhone") TossMobilePhoneInfo mobilePhone,
    @JsonProperty("giftCertificate") TossGiftCertificateInfo giftCertificate,
    @JsonProperty("discount") TossDiscountInfo discount,
    @JsonProperty("country") String country,
    @JsonProperty("failure") TossFailureInfo failure,
    @JsonProperty("isPartialCancelable") Boolean isPartialCancelable,
    @JsonProperty("receipt") Object receipt,  // ✅ Object로 변경 (URL 또는 객체일 수 있음)
    @JsonProperty("transactionKey") String transactionKey,
    @JsonProperty("currency") String currency
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossCardInfo(
      @JsonProperty("company") String company,
      @JsonProperty("number") String number,
      @JsonProperty("installmentPlanMonths") Integer installmentPlanMonths,  // ✅ Integer로 변경
      @JsonProperty("isInterestFree") Boolean isInterestFree,  // ✅ Boolean으로 변경
      @JsonProperty("approveNo") String approveNo,
      @JsonProperty("useCardPoint") Boolean useCardPoint,  // ✅ Boolean으로 변경
      @JsonProperty("cardType") String cardType,
      @JsonProperty("ownerType") String ownerType,
      @JsonProperty("acquireStatus") String acquireStatus,
      @JsonProperty("receiptUrl") String receiptUrl
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossVirtualAccountInfo(
      @JsonProperty("accountType") String accountType,
      @JsonProperty("accountNumber") String accountNumber,
      @JsonProperty("bankCode") String bankCode,
      @JsonProperty("customerName") String customerName,
      @JsonProperty("dueDate") OffsetDateTime dueDate,  // ✅ OffsetDateTime으로 변경
      @JsonProperty("refundStatus") String refundStatus,
      @JsonProperty("expired") Boolean expired,
      @JsonProperty("settlementStatus") String settlementStatus
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossTransferInfo(
      @JsonProperty("bankCode") String bankCode,
      @JsonProperty("settlementStatus") String settlementStatus
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossMobilePhoneInfo(
      @JsonProperty("customerMobilePhone") String customerMobilePhone,
      @JsonProperty("settlementStatus") String settlementStatus,
      @JsonProperty("receiptUrl") String receiptUrl
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossGiftCertificateInfo(
      @JsonProperty("approveNo") String approveNo,
      @JsonProperty("settlementStatus") String settlementStatus
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossDiscountInfo(
      @JsonProperty("amount") Long amount
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossFailureInfo(
      @JsonProperty("code") String code,
      @JsonProperty("message") String message
  ) {}
}