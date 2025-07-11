package org.example.studiopick.application.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import org.example.studiopick.config.SafeDateTimeDeserializer;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentCancelResponse(
    String paymentKey,
    String orderId,
    String orderName,
    String status,
    
    // ✅ 커스텀 Deserializer 적용
    @JsonDeserialize(using = SafeDateTimeDeserializer.SafeOffsetDateTimeDeserializer.class)
    OffsetDateTime requestedAt,
    
    @JsonDeserialize(using = SafeDateTimeDeserializer.SafeOffsetDateTimeDeserializer.class)
    OffsetDateTime approvedAt,
    
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
    String version,
    
    // ✅ transactionKey 필드 추가 (최상위 레벨에도 있을 수 있음)
    String transactionKey
) {


  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossCancelInfo(
      Long cancelAmount,
      String cancelReason,
      Long taxFreeAmount,
      Integer taxExemptionAmount,
      Long refundableAmount,
      
      // ✅ 커스텀 Deserializer 적용
      @JsonDeserialize(using = SafeDateTimeDeserializer.SafeOffsetDateTimeDeserializer.class)
      OffsetDateTime canceledAt,
      
      String transactionKey
  ) {}
}
