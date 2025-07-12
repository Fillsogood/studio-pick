package org.example.studiopick.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SettlementResponseDto {
    private Long settlementId;
    private Long paymentId;
    private BigDecimal totalAmount;
    private BigDecimal platformFee;
    private BigDecimal payoutAmount;
    private BigDecimal taxAmount;
    private String status;
    private LocalDateTime settledAt;
}
