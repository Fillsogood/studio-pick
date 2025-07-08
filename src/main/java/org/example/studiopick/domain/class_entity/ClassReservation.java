package org.example.studiopick.domain.class_entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.ClassReservationStatus;
import org.example.studiopick.domain.user.entity.User;

@Entity
@Table(name = "\"ClassReservation\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassReservation extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClassReservationStatus status = ClassReservationStatus.PENDING;
    
    @Builder
    public ClassReservation(User user, ClassEntity classEntity, ClassReservationStatus status) {
        this.user = user;
        this.classEntity = classEntity;
        this.status = status != null ? status : ClassReservationStatus.PENDING;
    }
    
    public void changeStatus(ClassReservationStatus status) {
        this.status = status;
    }
    
    public void confirm() {
        this.status = ClassReservationStatus.CONFIRMED;
    }
    
    public void cancel() {
        this.status = ClassReservationStatus.CANCELLED;
    }
    
    public boolean isConfirmed() {
        return this.status == ClassReservationStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return this.status == ClassReservationStatus.CANCELLED;
    }
    
    public boolean isPending() {
        return this.status == ClassReservationStatus.PENDING;
    }
}
