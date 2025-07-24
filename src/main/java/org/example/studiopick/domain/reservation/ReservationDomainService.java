package org.example.studiopick.domain.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ReservationDomainService {

  private final SystemSettingUtils settingUtils;
  private final JpaReservationRepository reservationRepository;

  /**
   * 예약 시간 중복 검증
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
   * 취소 가능 시간 내 여부 확인
   */
  public boolean isWithinCancellationPeriod(LocalDateTime reservationDateTime) {
    int cancelHours = settingUtils.getIntegerSetting("reservation.cancel.hours", 24);
    LocalDateTime cancellationDeadline = reservationDateTime.minusHours(cancelHours);
    return LocalDateTime.now().isBefore(cancellationDeadline);
  }

  /**
   * 예약 인원 수 유효성 검증
   */
  public boolean isValidPeopleCount(int peopleCount) {
    int maxPeople = settingUtils.getIntegerSetting("reservation.max.people", 20);
    return peopleCount >= 1 && peopleCount <= maxPeople;
  }

  /**
   * 예약 가능한 미래 날짜 검증
   */
  public boolean isValidAdvanceReservation(LocalDate reservationDate) {
    int maxAdvanceDays = settingUtils.getIntegerSetting("reservation.advance.days", 90);
    LocalDate maxDate = LocalDate.now().plusDays(maxAdvanceDays);
    return !reservationDate.isAfter(maxDate);
  }

  /**
   * 예약 시간 길이 유효성 검증
   * - ✅ 최소 시간만 체크 (상한 제한 제거)
   */
  public boolean isValidReservationDuration(LocalTime startTime, LocalTime endTime) {
    if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
      return false;
    }

    long hours = Duration.between(startTime, endTime).toHours();
    int minHours = settingUtils.getIntegerSetting("reservation.min.hours", 1);

    return hours >= minHours;
  }
}
