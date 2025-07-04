package org.example.studiopick.domain.payment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.SettlementStatus;
import org.example.studiopick.domain.studio.Studio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"Settlement\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id")
    private Studio studio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;
    
    @Column(name = "payout_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payoutAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;
    
    @Column(name = "settled_at")
    private LocalDateTime settledAt;
    
    @Builder
    public Settlement(Studio studio, Payment payment, BigDecimal totalAmount, 
                     BigDecimal platformFee, BigDecimal payoutAmount, SettlementStatus settlementStatus) {
        this.studio = studio;
        this.payment = payment;
        this.totalAmount = totalAmount;
        this.platformFee = platformFee;
        this.payoutAmount = payoutAmount;
        this.settlementStatus = settlementStatus != null ? settlementStatus : SettlementStatus.PENDING;
    }
    
    public void updateAmounts(BigDecimal totalAmount, BigDecimal platformFee, BigDecimal payoutAmount) {
        this.totalAmount = totalAmount;
        this.platformFee = platformFee;
        this.payoutAmount = payoutAmount;
    }
    
    public void changeStatus(SettlementStatus status) {
        this.settlementStatus = status;
    }
    
    public void markAsPaid() {
        this.settlementStatus = SettlementStatus.PAID;
        this.settledAt = LocalDateTime.now();
    }
    
    public void hold() {
        this.settlementStatus = SettlementStatus.HOLD;
    }
    
    public boolean isPaid() {
        return this.settlementStatus == SettlementStatus.PAID;
    }
    
    public boolean isOnHold() {
        return this.settlementStatus == SettlementStatus.HOLD;
    }
    
    public boolean isPending() {
        return this.settlementStatus == SettlementStatus.PENDING;
    }
}
