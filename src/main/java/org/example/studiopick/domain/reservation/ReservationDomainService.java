package org.example.studiopick.domain.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ReservationDomainService {

  private final ReservationRepository reservationRepository;

  public void validateOverlapping(Long studioId, LocalDate date, ReservationStatus status, LocalTime start, LocalTime end) {
    boolean exists = reservationRepository.existsOverlappingReservation(studioId, date, status, start, end);
    if (exists) {
      throw new IllegalStateException("해당 시간에 이미 예약이 존재합니다.");
    }
  }

  //예약 id로 예약 조회 (없으면 예외 발생)
  public Reservation findById(Long id){
    return reservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));
  }
}
