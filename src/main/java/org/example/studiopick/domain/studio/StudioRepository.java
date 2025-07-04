package org.example.studiopick.domain.studio;

import org.example.studiopick.domain.reservation.Reservation;

import java.util.Optional;

public interface StudioRepository {
  public Optional<Studio> findById(Long id);
}
