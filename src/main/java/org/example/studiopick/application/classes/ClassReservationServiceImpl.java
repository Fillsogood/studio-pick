package org.example.studiopick.application.classes;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classes.dto.ClassReservationCancelResponse;
import org.example.studiopick.application.classes.dto.UserClassReservationDto;
import org.example.studiopick.application.classes.dto.UserClassReservationListResponse;
import org.example.studiopick.domain.class_entity.ClassEntity;
import org.example.studiopick.domain.class_entity.ClassReservation;
import org.example.studiopick.domain.common.enums.ClassReservationStatus;
import org.example.studiopick.infrastructure.classes.ClassReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassReservationServiceImpl implements ClassReservationService {

  private final ClassReservationRepository classReservationRepository;

  @Override
  public UserClassReservationListResponse getUserReservations(Long userId, String status) {
    ClassReservationStatus reservationStatus = ClassReservationStatus.valueOf(status.toUpperCase());

    List<UserClassReservationDto> list = classReservationRepository
        .findByUserIdAndStatus(userId, reservationStatus).stream()
        .map(reservation -> {
          ClassEntity clazz = reservation.getClassEntity();
          int participants = clazz.getReservations().size();
          BigDecimal totalAmount = clazz.getPrice().multiply(BigDecimal.valueOf(participants));

          return new UserClassReservationDto(
              reservation.getId(),
              clazz.getTitle(),
              clazz.getInstructor(),
              clazz.getDate(),
              clazz.getStartTime(),
              clazz.getStudio().getName(),
              participants,
              totalAmount,
              reservation.getStatus().name().toLowerCase()
          );
        }).toList();

    return new UserClassReservationListResponse(list);
  }

  @Override
  @Transactional
  public void cancelReservation(Long reservationId, Long userId) {
    ClassReservation reservation = classReservationRepository.findById(reservationId)
        .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

    if (!reservation.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
    }

    // 예약이 이미 취소되었거나 완료된 경우 예외 처리
    if (reservation.isCancelled()) {
      throw new IllegalStateException("이미 취소된 예약입니다.");
    }

    if (!reservation.getClassEntity().isOpen()) {
      throw new IllegalStateException("클래스가 이미 종료되어 취소할 수 없습니다.");
    }

    // 시작 12시간 전까지 취소 가능
    LocalDateTime classStart = LocalDateTime.of(
        reservation.getClassEntity().getDate(),
        reservation.getClassEntity().getStartTime()
    );

    if (classStart.isBefore(LocalDateTime.now().plusHours(12))) {
      throw new IllegalStateException("클래스 시작 12시간 전까지만 취소가 가능합니다.");
    }

    reservation.cancel(); // 상태를 CANCELLED로 변경
    classReservationRepository.save(reservation);
  }
}
