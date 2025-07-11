package org.example.studiopick.application.reservation.dto;

import java.math.BigDecimal;

/**
 * 환불 정보
 */
public record RefundInfo(
    BigDecimal originalAmount,     // 원래 결제 금액
    BigDecimal cancellationFee,    // 취소 수수료
    BigDecimal refundAmount,       // 실제 환불 금액
    String policy                  // ✅ 필드명 변경: feePolicy -> policy
) {
    
    public static RefundInfo of(BigDecimal originalAmount, BigDecimal cancellationFee, String policy) {
        BigDecimal refundAmount = originalAmount.subtract(cancellationFee);
        return new RefundInfo(originalAmount, cancellationFee, refundAmount, policy);
    }
    
    public static RefundInfo fullRefund(BigDecimal originalAmount) {
        return new RefundInfo(originalAmount, BigDecimal.ZERO, originalAmount, "전액 환불");
    }
}
