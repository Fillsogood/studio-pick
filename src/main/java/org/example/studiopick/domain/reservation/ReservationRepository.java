package org.example.studiopick.domain.reservation;

import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository {
  boolean existsOverlappingReservation(Long studioId, LocalDate date, ReservationStatus status, LocalTime start, LocalTime end);


  List<Reservation> findByStudioIdAndReservationDateAndStatus(Long studioId, LocalDate reservationDate, ReservationStatus reservationStatus);

  Page<Reservation> findByUserIdOrderByReservationDateDesc(
      Long userId,
      Pageable pageable
  );
}