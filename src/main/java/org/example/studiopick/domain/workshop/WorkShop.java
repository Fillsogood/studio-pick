package org.example.studiopick.domain.workshop;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.HideStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"workshop\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkShop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Getter
    @OneToMany(mappedBy = "workShop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkShopImage> images = new ArrayList<>();

    @Column(name = "title", nullable = false, length = 30)
    private String title;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "instructor", length = 50)
    private String instructor;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "hide_status", nullable = false)
    private HideStatus hideStatus = HideStatus.OPEN;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();
    
    @Builder
    public WorkShop(User owner,String title, String description, BigDecimal price,
                    LocalDate date, String instructor, LocalTime startTime, LocalTime endTime, String thumbnailUrl, HideStatus status) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.price = price;
        this.date = date;
        this.instructor = instructor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.thumbnailUrl = thumbnailUrl;
        this.hideStatus = status != null ? status : HideStatus.OPEN;
    }
    
    public void updateBasicInfo(String title, String description, BigDecimal price) {
        this.title = title;
        this.description = description;
        this.price = price;
    }
    
    public void updateSchedule(LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public void WorkShopChangeStatus(HideStatus status) {
        this.hideStatus = status;
    }
    
    public void open() {
        this.hideStatus = HideStatus.OPEN;
    }
    
    public void close() {
        this.hideStatus = HideStatus.CLOSED;
    }
    
    public boolean isOpen() {
        return this.hideStatus == HideStatus.OPEN;
    }
    
    public boolean isClosed() {
        return this.hideStatus == HideStatus.CLOSED;
    }
    
    public void report() {
        this.hideStatus = HideStatus.REPORTED;
    }
    
    public void restore() {
        this.hideStatus = HideStatus.OPEN;
    }
    
    public boolean isReported() {
        return this.hideStatus == HideStatus.REPORTED;
    }
    
    public boolean isAvailableForReservation() {
        return this.hideStatus == HideStatus.OPEN && this.date != null &&
               this.date.isAfter(LocalDate.now().minusDays(1));
    }
    
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

}
