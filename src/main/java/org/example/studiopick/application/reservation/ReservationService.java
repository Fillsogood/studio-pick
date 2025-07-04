package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.dto.ReservationCreateCommand;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;
import org.example.studiopick.domain.reservation.ReservationRepository;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.StudioRepository;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ReservationService.java
@Service
@RequiredArgsConstructor
public class ReservationService {
  private final ReservationDomainService reservationDomainService;
  private final ReservationRepository reservationRepository;
  private final StudioRepository studioRepository;
  private final UserRepository userRepository;

  @Transactional
  public ReservationResponse create(ReservationCreateCommand command) {
    reservationDomainService.validateOverlapping(
        command.studioId(),
        command.reservationDate(),
        ReservationStatus.CONFIRMED,
        command.startTime(),
        command.endTime()
    );

    Studio studio = studioRepository.findById(command.studioId())
        .orElseThrow(() -> new IllegalArgumentException("해당 Studio id를 찾을 수 없습니다."));

    User user = userRepository.findById(command.userId())
        .orElseThrow(() -> new IllegalArgumentException("해당 User id를 찾을 수 없습니다."));

    // Studio 엔티티 주입
    Reservation reservation = Reservation.builder()
        .studio(studio)
        .user(user)
        .reservationDate(command.reservationDate())
        .startTime(command.startTime())
        .endTime(command.endTime())
        .status(ReservationStatus.CONFIRMED)
        .peopleCount(command.peopleCount())
        .build();

    Reservation saved = reservationRepository.save(reservation);

    return new ReservationResponse(
        saved.getId(),
        saved.getTotalAmount(),
        saved.getStatus()
    );
  }
}

