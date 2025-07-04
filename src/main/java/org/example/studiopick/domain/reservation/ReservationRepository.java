package org.example.studiopick.domain.reservation;

import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface ReservationRepository {
  boolean existsOverlappingReservation(Long studioId, LocalDate date, ReservationStatus status, LocalTime start, LocalTime end);

  Reservation save(Reservation reservation);

  Optional<Reservation> findById(Long id);
}
