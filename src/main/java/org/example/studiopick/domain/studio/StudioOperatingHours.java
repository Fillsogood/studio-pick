package org.example.studiopick.domain.studio;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.Weekday;

import java.time.LocalTime;

@Entity
@Table(name = "\"Studio_Operating_Hours\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudioOperatingHours extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "weekday", nullable = false)
    private Weekday weekday;
    
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;
    
    @Builder
    public StudioOperatingHours(Studio studio, Weekday weekday, LocalTime openTime, LocalTime closeTime) {
        this.studio = studio;
        this.weekday = weekday;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
    
    public void updateOperatingHours(LocalTime openTime, LocalTime closeTime) {
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
    
    public boolean isValidTime() {
        return openTime != null && closeTime != null && openTime.isBefore(closeTime);
    }

    public void setStudio(Studio studio) {this.studio = studio;}
}
