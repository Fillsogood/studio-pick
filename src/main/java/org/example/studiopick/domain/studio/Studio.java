package org.example.studiopick.domain.studio;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.user.User;

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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudioStatus status = StudioStatus.PENDING;
    
    @OneToOne(mappedBy = "studio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudioCommission commission;
    
    @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudioOperatingHours> operatingHours = new ArrayList<>();
    
    @Builder
    public Studio(User owner, String name, String description, String phone, String location, StudioStatus status) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.phone = phone;
        this.location = location;
        this.status = status != null ? status : StudioStatus.PENDING;
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
}
