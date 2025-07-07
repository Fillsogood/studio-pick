package org.example.studiopick.domain.payment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.PaymentMethod;
import org.example.studiopick.domain.common.enums.PaymentStatus;
import org.example.studiopick.domain.reservation.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PAID;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Builder
    public Payment(Reservation reservation, BigDecimal amount, PaymentMethod method, 
                   PaymentStatus status, LocalDateTime paidAt) {
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
        this.paidAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
    
    public void refund() {
        this.status = PaymentStatus.REFUNDED;
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
}
