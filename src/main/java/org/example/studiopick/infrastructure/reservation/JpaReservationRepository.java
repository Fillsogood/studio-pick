package org.example.studiopick.infrastructure.reservation;

import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    // ✅ 핵심 비즈니스 로직만 유지
    
    /**
     * 예약 시간 중복 검증 - 가장 중요한 비즈니스 규칙
     */
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.studio.id = :studioId
          AND r.reservationDate = :date
          AND r.status = :status
          AND (r.startTime < :end AND r.endTime > :start)
    """)
    boolean existsOverlappingReservation(
        @Param("studioId") Long studioId,
        @Param("date") LocalDate date,
        @Param("status") ReservationStatus status,
        @Param("start") LocalTime start,
        @Param("end") LocalTime end
    );

    /**
     * 스튜디오의 특정 날짜 예약 조회 - 가용 시간 계산용
     */
    List<Reservation> findByStudioIdAndReservationDateAndStatus(
        Long studioId, LocalDate date, ReservationStatus status);

    /**
     * 상태별 예약 수 - 통계용
     */
    long countByStatus(ReservationStatus status);

    /**
     * 특정 날짜 예약 수 - 통계용
     */
    long countByReservationDate(LocalDate date);
}
