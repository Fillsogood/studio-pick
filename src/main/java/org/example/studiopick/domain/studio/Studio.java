package org.example.studiopick.domain.studio;

import jakarta.persistence.*;
import lombok.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.user.entity.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"Studio\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Studio extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "hourly_base_rate", nullable = false)
    private Long hourlyBaseRate = 30000L;  // 기본값

    @Column(name = "per_person_rate", nullable = false)
    private Long perPersonRate = 5000L;    // 기본값

    @Column(name = "max_people")
    private Integer maxPeople = 10;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudioStatus status = StudioStatus.PENDING;

    @Column(name = "weekend_price", nullable = false)
    private BigDecimal weekendPrice;

    @OneToOne(mappedBy = "studio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudioCommission commission;
    
    @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudioOperatingHours> operatingHours = new ArrayList<>();

    @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL)
    private List<StudioImage> images = new ArrayList<>();

    @Builder
    public Studio(User owner, String name, String description, String phone, String location,
                  StudioStatus status, BigDecimal weekendPrice,
                  Long hourlyBaseRate, Long perPersonRate, Integer maxPeople) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.phone = phone;
        this.location = location;
        this.status = status != null ? status : StudioStatus.PENDING;
        this.weekendPrice = weekendPrice != null ? weekendPrice : BigDecimal.ZERO;

        // 기본값 설정 (SystemSettingUtils 없이)
        this.hourlyBaseRate = hourlyBaseRate != null ? hourlyBaseRate : 30000L;
        this.perPersonRate = perPersonRate != null ? perPersonRate : 5000L;
        this.maxPeople = maxPeople != null ? maxPeople : 10;
    }
    
    /**
     * 시스템 설정 기반 초기화를 위한 별도 메서드
     */
    public void initializeWithSystemSettings(SystemSettingUtils settingUtils) {
        if (settingUtils != null) {
            if (this.hourlyBaseRate == 30000L) {  // 기본값인 경우만 변경
                this.hourlyBaseRate = settingUtils.getIntegerSetting("studio.default.hourly.rate", 30000).longValue();
            }
            if (this.perPersonRate == 5000L) {  // 기본값인 경우만 변경
                this.perPersonRate = settingUtils.getIntegerSetting("studio.default.per.person.rate", 5000).longValue();
            }
            if (this.maxPeople == 10) {  // 기본값인 경우만 변경
                this.maxPeople = settingUtils.getIntegerSetting("studio.default.max.people", 10);
            }
        }
    }
    
    public void updateBasicInfo(String name, String description, String phone, String location) {
        this.name = name;
        this.description = description;
        this.phone = phone;
        this.location = location;
    }
    
    public void changeStatus(StudioStatus status) {
        this.status = status;
    }
    
    public void approve() {
        this.status = StudioStatus.APPROVED;
    }
    
    public void deactivate() {
        this.status = StudioStatus.INACTIVE;
    }
    
    public boolean isApproved() {
        return this.status == StudioStatus.APPROVED;
    }
    
    public boolean isActive() {
        return this.status == StudioStatus.APPROVED;
    }

    public void addImage(StudioImage image) {
        image.setStudio(this);
        images.add(image);
    }

    // 🆕 관리자용 개별 업데이트 메서드들
    public void updateName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = phone;
        }
    }

    public void updateLocation(String location) {
        if (location != null && !location.trim().isEmpty()) {
            this.location = location;
        }
    }

    public void updateHourlyBaseRate(Long hourlyBaseRate) {
        if (hourlyBaseRate != null && hourlyBaseRate >= 0) {
            this.hourlyBaseRate = hourlyBaseRate;
        }
    }

    public void updateWeekendPrice(BigDecimal weekendPrice) {
            this.weekendPrice = weekendPrice;
        }
    

    public void updatePerPersonRate(Long perPersonRate) {
        if (perPersonRate != null && perPersonRate >= 0) {
            this.perPersonRate = perPersonRate;
        }
    }

    // 🆕 관리자용 상태 변경 메서드들
    public void updateStatus(StudioStatus status) {
        this.status = status;
    }

    public void reject() {
        this.status = StudioStatus.REJECTED;
    }

    public void suspend() {
        this.status = StudioStatus.SUSPENDED;
    }

    public void activate() {
        this.status = StudioStatus.ACTIVE;
    }

    // 🆕 상태 확인 메서드들
    public boolean isPending() {
        return this.status == StudioStatus.PENDING;
    }

    public boolean isSuspended() {
        return this.status == StudioStatus.SUSPENDED;
    }

    public boolean isRejected() {
        return this.status == StudioStatus.REJECTED;
    }

    // 스튜디오 관리
    public void updateInfo(String description, String phone, Long hourlyBaseRate, BigDecimal weekend, Long perPersonRate, Integer maxPeople) {
        this.description = description;
        this.phone = phone;
        this.hourlyBaseRate = hourlyBaseRate;
        this.weekendPrice = weekend;
        this.perPersonRate = perPersonRate;
        this.maxPeople = maxPeople;
    }

    public void updateMaxPeople(Integer maxPeople) {
        this.maxPeople = maxPeople;
    }

    public void addOperatingHour(StudioOperatingHours operatingHour) {
        operatingHour.setStudio(this);  // 양방향 설정
        this.operatingHours.add(operatingHour);
    }
}
