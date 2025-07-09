package org.example.studiopick.application.classes;

import org.example.studiopick.application.classes.dto.ClassReservationCancelResponse;
import org.example.studiopick.application.classes.dto.UserClassReservationListResponse;

public interface ClassReservationService {
  UserClassReservationListResponse getUserReservations(Long userId, String status);
  void cancelReservation(Long reservationId, Long userId);
}
