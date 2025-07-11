package org.example.studiopick.domain.refund;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.RefundStatus;
import org.example.studiopick.domain.payment.Payment;
import org.example.studiopick.domain.reservation.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 내역 엔티티
 * 모든 환불 이력을 추적하기 위한 엔티티
 */
@Entity
@Table(name = "refund")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    
    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;        // 실제 환불 금액
    
    @Column(name = "original_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalAmount;      // 원래 결제 금액
    
    @Column(name = "cancellation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal cancellationFee;     // 취소 수수료
    
    @Column(name = "refund_reason", nullable = false, length = 500)
    private String refundReason;            // 환불 사유
    
    @Column(name = "refund_policy", nullable = false, length = 200)
    private String refundPolicy;            // 적용된 환불 정책
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefundStatus status;            // 환불 상태
    
    @Column(name = "toss_payment_key", length = 200)
    private String tossPaymentKey;          // 토스페이먼츠 결제 키
    
    @Column(name = "toss_transaction_key", length = 200)  
    private String tossTransactionKey;      // 토스페이먼츠 거래 키
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;       // 환불 완료 시간
    
    @Column(name = "failure_reason", length = 1000)
    private String failureReason;           // 환불 실패 사유
    
    @Builder
    public Refund(Payment payment, Reservation reservation, BigDecimal refundAmount, 
                  BigDecimal originalAmount, BigDecimal cancellationFee, 
                  String refundReason, String refundPolicy, RefundStatus status,
                  String tossPaymentKey) {
        this.payment = payment;
        this.reservation = reservation;
        this.refundAmount = refundAmount;
        this.originalAmount = originalAmount;
        this.cancellationFee = cancellationFee;
        this.refundReason = refundReason;
        this.refundPolicy = refundPolicy;
        this.status = status != null ? status : RefundStatus.PENDING;
        this.tossPaymentKey = tossPaymentKey;
    }
    
    /**
     * 환불 처리 중 상태로 변경
     */
    public void markAsProcessing() {
        this.status = RefundStatus.PROCESSING;
    }
    
    /**
     * 환불 완료 처리
     */
    public void markAsCompleted(String tossTransactionKey) {
        this.status = RefundStatus.COMPLETED;
        this.tossTransactionKey = tossTransactionKey;
        this.refundedAt = LocalDateTime.now();
    }
    
    /**
     * 환불 실패 처리
     */
    public void markAsFailed(String failureReason) {
        this.status = RefundStatus.FAILED;
        this.failureReason = failureReason;
    }
    
    /**
     * 환불율 계산
     */
    public BigDecimal getRefundRate() {
        if (originalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return refundAmount.divide(originalAmount, 4, java.math.RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 전액 환불 여부 확인
     */
    public boolean isFullRefund() {
        return refundAmount.compareTo(originalAmount) == 0;
    }
    
    /**
     * 부분 환불 여부 확인
     */
    public boolean isPartialRefund() {
        return refundAmount.compareTo(BigDecimal.ZERO) > 0 && 
               refundAmount.compareTo(originalAmount) < 0;
    }
}
