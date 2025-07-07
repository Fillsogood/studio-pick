package org.example.studiopick.domain.class_entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.ClassStatus;
import org.example.studiopick.domain.studio.Studio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"Class\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;
    
    @Column(name = "title", nullable = false, length = 30)
    private String title;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "date")
    private LocalDate date;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClassStatus status = ClassStatus.OPEN;
    
    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassReservation> reservations = new ArrayList<>();
    
    @Builder
    public ClassEntity(Studio studio, String title, String description, BigDecimal price, 
                      LocalDate date, LocalTime startTime, LocalTime endTime, ClassStatus status) {
        this.studio = studio;
        this.title = title;
        this.description = description;
        this.price = price;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status != null ? status : ClassStatus.OPEN;
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
    
    public void changeStatus(ClassStatus status) {
        this.status = status;
    }
    
    public void open() {
        this.status = ClassStatus.OPEN;
    }
    
    public void close() {
        this.status = ClassStatus.CLOSED;
    }
    
    public boolean isOpen() {
        return this.status == ClassStatus.OPEN;
    }
    
    public boolean isClosed() {
        return this.status == ClassStatus.CLOSED;
    }
    
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}
