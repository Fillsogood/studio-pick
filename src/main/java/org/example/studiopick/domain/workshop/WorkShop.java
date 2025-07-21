package org.example.studiopick.domain.workshop;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.WorkShopStatus;
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

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private WorkShopStatus status;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    @Builder
    public WorkShop(User owner, String title, String description, BigDecimal price,
                    LocalDate date, String instructor, LocalTime startTime, LocalTime endTime,
                    String thumbnailUrl, WorkShopStatus status, String address) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.price = price;
        this.date = date;
        this.instructor = instructor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.thumbnailUrl = thumbnailUrl;
//        this.address = address;// 원래코드
        this.address = (address != null) ? address : ""; //null 방지
        this.status = status != null ? status : WorkShopStatus.PENDING;
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

    public List<String> getImageUrls() {
        return images.stream()
                .map(WorkShopImage::getImageUrl)
                .toList();
    }


    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    public boolean isAvailableForReservation() {
        return this.status == WorkShopStatus.ACTIVE && this.date != null &&
                this.date.isAfter(LocalDate.now().minusDays(1));
    }

    public void activate() {
        this.status = WorkShopStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = WorkShopStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == WorkShopStatus.ACTIVE;
    }

    public boolean isPending() {
        return this.status == WorkShopStatus.PENDING;
    }

    public boolean isInactive() {
        return this.status == WorkShopStatus.INACTIVE;
    }

    public void changeStatus(WorkShopStatus status) {
        this.status = status;
    }

}
