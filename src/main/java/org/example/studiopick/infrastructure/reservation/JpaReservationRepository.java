package org.example.studiopick.infrastructure.reservation;

import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;


public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepository {

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

  Page<Reservation> findByUserIdOrderByReservationDateDesc(Long userId, Pageable pageable);

  // 상태별 필터링
  Page<Reservation> findByUserIdAndStatusOrderByReservationDateDesc(Long userId, ReservationStatus status, Pageable pageable);

  // 날짜 범위 필터링
  Page<Reservation> findByUserIdAndReservationDateBetweenOrderByReservationDateDesc(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

  // 상태 + 날짜 범위 필터링
  Page<Reservation> findByUserIdAndStatusAndReservationDateBetweenOrderByReservationDateDesc(Long userId, ReservationStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);

  // 스튜디오별 필터링
  Page<Reservation> findByUserIdAndStudioIdOrderByReservationDateDesc(Long userId, Long studioId, Pageable pageable);

  // 모든 필터 조합
  Page<Reservation> findByUserIdAndStatusAndReservationDateBetweenAndStudioIdOrderByReservationDateDesc(Long userId, ReservationStatus status, LocalDate startDate, LocalDate endDate, Long studioId, Pageable pageable);

}
