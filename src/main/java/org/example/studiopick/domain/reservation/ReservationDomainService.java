// src/main/java/org/example/studiopick/domain/reservation/ReservationDomainService.java 수정
package org.example.studiopick.domain.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ReservationDomainService {

  private final SystemSettingUtils settingUtils;
  private final JpaReservationRepository reservationRepository; // 추가

  /**
   * 예약 시간 중복 검증
   * @param studioId 스튜디오 ID
   * @param reservationDate 예약 날짜
   * @param status 확인할 예약 상태
   * @param startTime 시작 시간
   * @param endTime 종료 시간
   * @throws IllegalArgumentException 중복 예약이 있는 경우
   */
  public void validateOverlapping(Long studioId, LocalDate reservationDate,
                                  ReservationStatus status, LocalTime startTime, LocalTime endTime) {
    boolean hasOverlapping = reservationRepository.existsOverlappingReservation(
        studioId, reservationDate, status, startTime, endTime);

    if (hasOverlapping) {
      throw new IllegalArgumentException("해당 시간대에 이미 예약이 존재합니다.");
    }
  }

  /**
   * 취소 가능 시간 내 여부 확인 (시스템 설정 기반)
   */
  public boolean isWithinCancellationPeriod(LocalDateTime reservationDateTime) {
    int cancelHours = settingUtils.getIntegerSetting("reservation.cancel.hours", 24);
    LocalDateTime cancellationDeadline = reservationDateTime.minusHours(cancelHours);
    return LocalDateTime.now().isBefore(cancellationDeadline);
  }

  /**
   * 예약 인원 수 유효성 검증 (시스템 설정 기반)
   */
  public boolean isValidPeopleCount(int peopleCount) {
    int maxPeople = settingUtils.getIntegerSetting("reservation.max.people", 20);
    return peopleCount >= 1 && peopleCount <= maxPeople;
  }

  /**
   * 예약 가능한 미래 날짜 검증 (시스템 설정 기반)
   */
  public boolean isValidAdvanceReservation(LocalDate reservationDate) {
    int maxAdvanceDays = settingUtils.getIntegerSetting("reservation.advance.days", 90);
    LocalDate maxDate = LocalDate.now().plusDays(maxAdvanceDays);
    return !reservationDate.isAfter(maxDate);
  }

  /**
   * 예약 시간 길이 유효성 검증 (시스템 설정 기반)
   */
  public boolean isValidReservationDuration(LocalTime startTime, LocalTime endTime) {
    if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
      return false;
    }

    long hours = java.time.Duration.between(startTime, endTime).toHours();
    int minHours = settingUtils.getIntegerSetting("reservation.min.hours", 1);
    int maxHours = settingUtils.getIntegerSetting("reservation.max.hours", 8);

    return hours >= minHours && hours <= maxHours;
  }
}