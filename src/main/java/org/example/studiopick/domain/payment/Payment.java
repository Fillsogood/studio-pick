package org.example.studiopick.domain.payment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.config.PaymentStatusConverter;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.PaymentMethod;
import org.example.studiopick.domain.common.enums.PaymentStatus;
import org.example.studiopick.domain.reservation.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "\"Payment\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private PaymentMethod method;

    @Convert(converter = PaymentStatusConverter.class)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PAID;
    
    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "payment_key")
    private String paymentKey;        // 토스 결제 고유 키

    @Column(name = "order_id")
    private String orderId;           // 주문 ID (우리가 생성)

    @Column(name = "transaction_key")
    private String transactionKey;    // 토스 거래 키

    @Column(name = "failure_code")
    private String failureCode;       // 실패 코드

    @Column(name = "failure_reason")
    private String failureReason;     // 실패 사유
    
    @Builder
    public Payment(Reservation reservation, BigDecimal amount, PaymentMethod method, 
                   PaymentStatus status,
                   OffsetDateTime paidAt) {
        this.reservation = reservation;
        this.amount = amount;
        this.method = method;
        this.status = status != null ? status : PaymentStatus.PAID;
        this.paidAt = paidAt;
    }
    
    public void updateAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public void changeStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public void markAsPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt =
            OffsetDateTime.now();
    }
    
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
    
    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }
    
    public void partialCancel(BigDecimal cancelAmount) {
        this.status = PaymentStatus.PARTIAL_CANCELED;
        // 부분 취소 금액을 별도로 추적하고 싶다면 필드 추가 가능
    }
    
    // 토스페이먼츠 연동을 위한 메서드들
    public void updateOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public void updatePaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }
    
    public void updateTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
    }
    
    public void updateMethod(PaymentMethod method) {
        this.method = method;
    }
    
    public void updateFailureInfo(String failureCode, String failureReason) {
        this.failureCode = failureCode;
        this.failureReason = failureReason;
    }
    
    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }
    
    public boolean isCancelled() {
        return this.status == PaymentStatus.CANCELLED;
    }
    
    public boolean isRefunded() {
        return this.status == PaymentStatus.REFUNDED;
    }
    
    public boolean isPartialCancelled() {
        return this.status == PaymentStatus.PARTIAL_CANCELED;
    }
    
    public boolean isCancellable() {
        return this.status == PaymentStatus.DONE || this.status == PaymentStatus.PAID;
    }
}
