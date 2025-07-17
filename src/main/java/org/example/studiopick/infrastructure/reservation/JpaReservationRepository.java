package org.example.studiopick.infrastructure.reservation;

import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

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

    // 시간 겹침 체크 (스튜디오)
    @Query("SELECT r FROM Reservation r WHERE r.studio.id = :studioId " +
           "AND r.reservationDate = :date " +
           "AND r.status NOT IN ('CANCELLED', 'REFUNDED') " +
           "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
    List<Reservation> findConflictingStudioReservations(
        @Param("studioId") Long studioId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
    
    // 시간 겹침 체크 (워크샵)
    @Query("SELECT r FROM Reservation r WHERE r.workshop.id = :workshopId " +
           "AND r.reservationDate = :date " +
           "AND r.status NOT IN ('CANCELLED', 'REFUNDED') " +
           "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
    List<Reservation> findConflictingWorkshopReservations(
        @Param("workshopId") Long workshopId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

    /**
     * 스튜디오의 특정 날짜 예약 조회 - 가용 시간 계산용
     */
    List<Reservation> findByStudioIdAndReservationDateAndStatus(
        Long studioId, LocalDate date, ReservationStatus status);

    // 특정 기간 내 예약 통계
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    long countReservationsBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 상태별 예약 수 - 통계용
     */
    long countByStatus(ReservationStatus status);

    /**
     * 특정 날짜 예약 수 - 통계용
     */
    long countByReservationDate(LocalDate date);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
