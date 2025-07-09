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
import java.time.LocalDateTime;
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

    @Column(name = "cancelled_reason")
    private String cancelledReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

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

    /**
     * 인원 수 업데이트 (검증 로직 제거 - 서비스에서 처리)
     */
    public void updatePeopleCount(Short peopleCount) {
        // ✅ 하드코딩된 20명 제한 제거 - ReservationDomainService에서 시스템 설정 기반 검증
        this.peopleCount = peopleCount;
    }

    public void updateCancelInfo(String reason) {
        this.cancelledReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void changeStatus(ReservationStatus status) {
        this.status = status;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    public void refund() {
        this.status = ReservationStatus.REFUNDED;
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

    // 취소 가능 여부 체크
    public boolean isCancellable() {
        return this.status == ReservationStatus.CONFIRMED;  // CONFIRMED만 취소 가능
    }


    /**
     * 취소 실행 (시간 검증은 서비스에서 사전 처리)
     */
    public void cancel(String reason) {
        if (!isCancellable()) {
            throw new IllegalStateException("취소할 수 없는 예약 상태입니다.");
        }
        this.status = ReservationStatus.CANCEL_REQUESTED;
        this.cancelledReason = reason;
    }

    /**
     * 취소 실행 (검증 없이 - 이미 모든 검증이 완료된 상태에서 호출)
     */
    public void cancelWithoutValidation(String reason) {
        this.status = ReservationStatus.CANCEL_REQUESTED;
        this.cancelledReason = reason;
    }

    // 스튜디오에서 취소 승인
    public void approveCancellation() {
        if (this.status != ReservationStatus.CANCEL_REQUESTED) {
            throw new IllegalStateException("취소 요청 상태가 아닙니다.");
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    // 스튜디오에서 취소 거부
    public void rejectCancellation() {
        if (this.status != ReservationStatus.CANCEL_REQUESTED) {
            throw new IllegalStateException("취소 요청 상태가 아닙니다.");
        }
        this.status = ReservationStatus.CONFIRMED;  // 다시 확정 상태로
    }
}