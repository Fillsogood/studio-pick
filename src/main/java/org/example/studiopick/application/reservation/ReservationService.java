package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.dto.AvailableTimesResponse;
import org.example.studiopick.application.reservation.dto.ReservationCreateCommand;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;
import org.example.studiopick.domain.reservation.ReservationRepository;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.StudioOperatingHoursRepository;
import org.example.studiopick.domain.studio.StudioRepository;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// ReservationService.java
@Service
@RequiredArgsConstructor
public class ReservationService {
  private final ReservationDomainService reservationDomainService;
  private final ReservationRepository reservationRepository;
  private final StudioRepository studioRepository;
  private final UserRepository userRepository;
  private final StudioOperatingHoursRepository studioOperatingHoursRepository;

  @Transactional
  public ReservationResponse create(Long studioId, ReservationCreateCommand command) {
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

  public AvailableTimesResponse getAvailableTimes(Long studioId, LocalDate date) {
    // 1. 해당 날짜 예약 조회
    List<Reservation> reservations = reservationRepository.findByStudioIdAndReservationDateAndStatus(
        studioId, date, ReservationStatus.CONFIRMED
    );

    // 2. 예약된 시간 리스트
    List<String> bookedTimes = reservations.stream()
        .sorted(Comparator.comparing(Reservation::getStartTime))
        .map(r -> r.getStartTime().toString())
        .toList();

    // 3. 예: 오전 9시 ~ 오후 6시 기준 가용 시간 생성
    LocalTime start = LocalTime.of(9, 0);
    LocalTime end = LocalTime.of(18, 0);

    List<String> allTimes = new ArrayList<>();
    LocalTime cursor = start;
    while (cursor.isBefore(end)) {
      allTimes.add(cursor.toString());
      cursor = cursor.plusHours(1);
    }

    // 4. 예약된 시간을 제외한 가용 시간
    List<String> availableTimes = allTimes.stream()
        .filter(t -> !bookedTimes.contains(t))
        .collect(Collectors.toList());

    return new AvailableTimesResponse(
        availableTimes,
        bookedTimes
    );
  }
}

