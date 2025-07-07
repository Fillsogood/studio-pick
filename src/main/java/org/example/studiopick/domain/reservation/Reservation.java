package org.example.studiopick.domain.reservation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "\"Reservation\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id")
    private Studio studio;
    
    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "people_count")
    private Short peopleCount;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @Builder
    public Reservation(User user, Studio studio, LocalDate reservationDate, 
                      LocalTime startTime, LocalTime endTime, Short peopleCount, ReservationStatus status, Long totalAmount) {
        this.user = user;
        this.studio = studio;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.peopleCount = peopleCount;
        this.status = status != null ? status : ReservationStatus.PENDING;
        this.totalAmount = totalAmount;
    }
    
    public void updateReservationTime(LocalDate reservationDate, LocalTime startTime, LocalTime endTime) {
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public void updatePeopleCount(Short peopleCount) {
        if (peopleCount != null && peopleCount >= 1 && peopleCount <= 20) {
            this.peopleCount = peopleCount;
        }
    }
    
    public void changeStatus(ReservationStatus status) {
        this.status = status;
    }
    
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }
    
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
    
    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }
    
    public boolean isConfirmed() {
        return this.status == ReservationStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return this.status == ReservationStatus.CANCELLED;
    }
    
    public boolean isCompleted() {
        return this.status == ReservationStatus.COMPLETED;
    }
    
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}
