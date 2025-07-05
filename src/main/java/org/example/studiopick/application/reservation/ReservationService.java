package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.dto.*;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.common.validator.UserValidator;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;

import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.user.UserRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private final JpaReservationRepository jpaReservationRepository;
  private final JpaStudioRepository jpaStudioRepository;
  private final UserRepository userRepository;
  private final UserValidator userValidator;
  private final PaginationValidator paginationValidator;

  @Transactional
  public ReservationResponse create(Long studioId, ReservationCreateCommand command) {

    // 스튜디오 동시성 처리를 위한 락 흭득
    Studio studio = jpaStudioRepository.findByIdWithLock(studioId)
        .orElseThrow(() -> new IllegalArgumentException("해당 Studio id를 찾을 수 없습니다."));

    reservationDomainService.validateOverlapping(
        command.studioId(),
        command.reservationDate(),
        ReservationStatus.CONFIRMED,
        command.startTime(),
        command.endTime()
    );

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
        .totalAmount(command.totalAmount())
        .build();

    Reservation saved = jpaReservationRepository.save(reservation);

    return new ReservationResponse(
        saved.getId(),
        saved.getTotalAmount(),
        saved.getStatus()
    );
  }

  public AvailableTimesResponse getAvailableTimes(Long studioId, LocalDate date) {
    // 1. 해당 날짜 예약 조회
    List<Reservation> reservations = jpaReservationRepository.findByStudioIdAndReservationDateAndStatus(
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

  public UserReservationListResponse getUserReservations(Long userId, int page, int size) {

    // 입력값 검증
    paginationValidator.validatePaginationParameters(page, size);
    userValidator.findAndValidateUser(userId);

    // 예약 조회 로직
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<Reservation> reservationsPage = jpaReservationRepository
        .findByUserIdOrderByReservationDateDesc(userId, pageable);

    List<UserReservationResponse> reservations = reservationsPage.getContent()
        .stream()
        .map(this::toUserReservationResponse)
        .toList();

    return new UserReservationListResponse(
        reservations,
        new PaginationResponse(page, reservationsPage.getTotalElements())
    );
  }

  private UserReservationResponse toUserReservationResponse(Reservation reservation) {
    return new UserReservationResponse(
        reservation.getId(),
        reservation.getStudio().getName(),
        reservation.getReservationDate().toString(),
        reservation.getStartTime().toString(),
        reservation.getEndTime().toString(),
        reservation.getStatus().name().toLowerCase(),
        reservation.getTotalAmount()
    );
  }

  public ReservationCancelResponse cancleReservation(Long id, ReservationCancelRequest request) {
    // 1.예약 조회 및 존재 확인
    Reservation reservation = jpaReservationRepository.findById(id)
        .orElseThrow(()-> new IllegalArgumentException("예약 Id를 찾을 수 없습니다."));

    // 2. 본인 예약 확인
    if (!reservation.getUser().getId().equals(request.userId())){
      throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
    }

    // 3. 취소
    reservation.cancel(request.reason());

    // 4. 저장
    Reservation saved = jpaReservationRepository.save(reservation);

    // 5. 응답 생성
    return new ReservationCancelResponse(
        saved.getId(),
        saved.getStatus(),
        LocalDateTime.now()
    );
  }
}

