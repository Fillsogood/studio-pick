package org.example.studiopick.domain.studio;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "\"Studio_Commission\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudioCommission {
    
    @Id
    @Column(name = "studio_id")
    private Long studioId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "studio_id")
    private Studio studio;
    
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.valueOf(10.00);
    
    @Builder
    public StudioCommission(Studio studio, BigDecimal commissionRate) {
        this.studio = studio;
        this.commissionRate = commissionRate != null ? commissionRate : BigDecimal.valueOf(10.00);
    }
    
    public void updateCommissionRate(BigDecimal rate) {
        if (rate != null && rate.compareTo(BigDecimal.ZERO) >= 0 && rate.compareTo(BigDecimal.valueOf(100)) <= 0) {
            this.commissionRate = rate;
        }
    }
}
