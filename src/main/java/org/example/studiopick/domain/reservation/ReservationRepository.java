package org.example.studiopick.domain.reservation;

import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
  boolean existsOverlappingReservation(Long studioId, LocalDate date, ReservationStatus status, LocalTime start, LocalTime end);

  List<Reservation> findByStudioIdAndReservationDateAndStatus(Long studioId, LocalDate reservationDate, ReservationStatus reservationStatus);

  Page<Reservation> findByUserIdOrderByReservationDateDesc(
      Long userId,
      Pageable pageable
  );

  // 관리자용 전체 조회 메서드들
  Page<Reservation> findAllByOrderByReservationDateDesc(Pageable pageable);

  // 상태별 조회
  Page<Reservation> findByStatusOrderByReservationDateDesc(ReservationStatus status, Pageable pageable);
  long countByStatus(ReservationStatus status);

  // 날짜별 조회
  Page<Reservation> findByReservationDateBetweenOrderByReservationDateDesc(
      LocalDate startDate, LocalDate endDate, Pageable pageable);
  long countByReservationDate(LocalDate date);

  // 복합 조건 조회
  Page<Reservation> findByStatusAndReservationDateBetweenOrderByReservationDateDesc(
      ReservationStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);

  Page<Reservation> findByStatusAndReservationDateBetweenAndUserIdOrderByReservationDateDesc(
      ReservationStatus status, LocalDate startDate, LocalDate endDate, Long userId, Pageable pageable);

  Page<Reservation> findByStatusAndReservationDateBetweenAndStudioIdOrderByReservationDateDesc(
      ReservationStatus status, LocalDate startDate, LocalDate endDate, Long studioId, Pageable pageable);

  Page<Reservation> findByStatusAndReservationDateBetweenAndUserIdAndStudioIdOrderByReservationDateDesc(
      ReservationStatus status, LocalDate startDate, LocalDate endDate, Long userId, Long studioId, Pageable pageable);

  // 스튜디오별 조회
  Page<Reservation> findByStudioIdOrderByReservationDateDesc(Long studioId, Pageable pageable);
  Page<Reservation> findByStudioIdAndStatusOrderByReservationDateDesc(Long studioId, ReservationStatus status, Pageable pageable);
}