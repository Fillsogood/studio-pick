package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.dto.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.common.validator.UserValidator;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
  private final ReservationDomainService reservationDomainService;
  private final JpaReservationRepository jpaReservationRepository;
  private final JpaStudioRepository jpaStudioRepository;
  private final JpaUserRepository jpauserRepository;
  private final UserValidator userValidator;
  private final PaginationValidator paginationValidator;
  private final SystemSettingUtils settingUtils;

  /**
   * 스튜디오 예약 생성
   * - 시스템 설정 기반 검증
   * - 도메인 서비스를 통한 비즈니스 규칙 검증
   * - 예약 시간 중복 검증
   * - 스튜디오/사용자 존재 확인
   * - 동시성 처리 (락 사용)
   * @param studioId 스튜디오 ID
   * @param command 예약 생성 요청 정보
   * @return 생성된 예약 정보
   */
  @Transactional
  public ReservationResponse create(Long studioId, ReservationCreateCommand command) {

    // 스튜디오 동시성 처리를 위한 락 흭득
    Studio studio = jpaStudioRepository.findByIdWithLock(studioId)
        .orElseThrow(() -> new IllegalArgumentException("해당 Studio id를 찾을 수 없습니다."));

    // 1. 도메인 서비스를 통한 비즈니스 규칙 검증
    validateReservationRules(command);

    // 2. 예약 시간 중복 검증
    reservationDomainService.validateOverlapping(
        command.studioId(),
        command.reservationDate(),
        ReservationStatus.PENDING,
        command.startTime(),
        command.endTime()
    );

    // 3. 사용자 존재 확인
    User user = jpauserRepository.findById(command.userId())
        .orElseThrow(() -> new IllegalArgumentException("해당 User id를 찾을 수 없습니다."));

    // 4. 요금 계산 및 최소 금액 검증
    Long totalAmount = calculateTotalAmount(studio, command.startTime(), command.endTime(), command.peopleCount());
    validateMinimumAmount(totalAmount);

    // 5. 예약 생성
    Reservation reservation = Reservation.builder()
        .studio(studio)
        .user(user)
        .reservationDate(command.reservationDate())
        .startTime(command.startTime())
        .endTime(command.endTime())
        .status(ReservationStatus.PENDING)
        .peopleCount(command.peopleCount())
        .totalAmount(totalAmount)
        .build();

    Reservation saved = jpaReservationRepository.save(reservation);

    return new ReservationResponse(
        saved.getId(),
        saved.getTotalAmount(),
        saved.getStatus()
    );
  }

  /**
   * 예약 생성 규칙 검증 (도메인 서비스 활용)
   */
  private void validateReservationRules(ReservationCreateCommand command) {
    // 1. 인원 수 검증 (Short -> int 변환)
    if (!reservationDomainService.isValidPeopleCount(command.peopleCount().intValue())) {
      int maxPeople = settingUtils.getIntegerSetting("reservation.max.people", 20);
      throw new IllegalArgumentException("예약 인원은 1명 이상 " + maxPeople + "명 이하여야 합니다.");
    }

    // 2. 미래 날짜 검증
    if (!reservationDomainService.isValidAdvanceReservation(command.reservationDate())) {
      int maxAdvanceDays = settingUtils.getIntegerSetting("reservation.advance.days", 90);
      throw new IllegalArgumentException("예약은 최대 " + maxAdvanceDays + "일 후까지만 가능합니다.");
    }

    // 3. 과거 날짜 검증
    if (command.reservationDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("과거 날짜로는 예약할 수 없습니다.");
    }

    // 4. 예약 시간 길이 검증
    if (!reservationDomainService.isValidReservationDuration(command.startTime(), command.endTime())) {
      int minHours = settingUtils.getIntegerSetting("reservation.min.hours", 1);
      int maxHours = settingUtils.getIntegerSetting("reservation.max.hours", 8);
      throw new IllegalArgumentException("예약 시간은 " + minHours + "시간 이상 " + maxHours + "시간 이하여야 합니다.");
    }
  }

  /**
   * 최소 결제 금액 검증 (시스템 설정 기반)
   */
  private void validateMinimumAmount(Long totalAmount) {
    int minAmount = settingUtils.getIntegerSetting("payment.min.amount", 10000);

    if (totalAmount < minAmount) {
      throw new IllegalArgumentException("최소 결제 금액은 " + minAmount + "원 입니다.");
    }
  }

  /**
   * 예약 총 금액 계산
   * @param studio 스튜디오 정보
   * @param startTime 시작 시간
   * @param endTime 종료 시간
   * @param peopleCount 인원 수
   * @return 총 금액
   */
  private Long calculateTotalAmount(Studio studio, LocalTime startTime, LocalTime endTime, Short peopleCount) {
    // 사용 시간 계산
    long hours = Duration.between(startTime, endTime).toHours();

    // 기본 요금 × 시간
    long baseAmount = studio.getHourlyBaseRate() * hours;

    // 인원 수 × 인당 요금 × 시간
    long personAmount = peopleCount * studio.getPerPersonRate() * hours;

    return baseAmount + personAmount;
  }

  /**
   * 예약 가능 시간 조회 (시스템 설정 기반 운영시간)
   * - 해당 날짜의 예약된 시간 조회
   * - 시스템 설정의 운영시간에서 예약된 시간 제외
   * @param studioId 스튜디오 ID
   * @param date 조회할 날짜
   * @return 예약 가능/불가능 시간 목록
   */
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

    // 3. 시스템 설정에서 운영시간 조회
    int startHour = settingUtils.getIntegerSetting("studio.operating.start.hour", 9);
    int endHour = settingUtils.getIntegerSetting("studio.operating.end.hour", 18);

    LocalTime start = LocalTime.of(startHour, 0);
    LocalTime end = LocalTime.of(endHour, 0);

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

  /**
   * 사용자별 예약 내역 조회 (필터링 지원)
   * - 상태별, 날짜범위별, 스튜디오별 필터링
   * - 최근 예약순 정렬, 페이지네이션
   * @param userId 사용자 ID
   * @param status 예약 상태 (선택적)
   * @param startDate 시작 날짜 (선택적)
   * @param endDate 종료 날짜 (선택적)
   * @param studioId 스튜디오 ID (선택적)
   * @return 필터링된 예약 내역 목록
   */
  public UserReservationListResponse getUserReservations(Long userId, int page, int size,
                                                         String status, LocalDate startDate, LocalDate endDate, Long studioId) {

    // 입력값 검증
    paginationValidator.validatePaginationParameters(page, size);
    userValidator.findAndValidateUser(userId);

    // 예약 조회 로직
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<Reservation> reservationsPage = getFilteredReservations(userId, status, startDate, endDate, studioId, pageable);

    List<UserReservationResponse> reservations = reservationsPage.getContent()
        .stream()
        .map(this::toUserReservationResponse)
        .toList();

    return new UserReservationListResponse(
        reservations,
        new PaginationResponse(page, reservationsPage.getTotalElements())
    );
  }

  /**
   * 필터 조건에 따른 예약 목록 조회
   * - 다양한 필터 조합 처리 (상태, 날짜범위, 스튜디오)
   * - 필터가 없으면 전체 조회
   * @param userId 사용자 ID
   * @param status 예약 상태 필터 (선택적)
   * @param startDate 시작 날짜 필터 (선택적)
   * @param endDate 종료 날짜 필터 (선택적)
   * @param studioId 스튜디오 ID 필터 (선택적)
   * @param pageable 페이지네이션 정보
   * @return 필터링된 예약 페이지 결과
   */
  private Page<Reservation> getFilteredReservations(Long userId, String status, LocalDate startDate, LocalDate endDate, Long studioId, Pageable pageable) {
    ReservationStatus reservationStatus = null;
    if (status != null && !status.trim().isEmpty()) {
      try {
        reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("잘못된 예약 상태입니다: " + status);
      }
    }

    // 모든 필터 조건이 있는 경우
    if (reservationStatus != null && startDate != null && endDate != null && studioId != null) {
      return jpaReservationRepository.findByUserIdAndStatusAndReservationDateBetweenAndStudioIdOrderByReservationDateDesc(
          userId, reservationStatus, startDate, endDate, studioId, pageable);
    }
    // 상태 + 날짜 범위
    else if (reservationStatus != null && startDate != null && endDate != null) {
      return jpaReservationRepository.findByUserIdAndStatusAndReservationDateBetweenOrderByReservationDateDesc(
          userId, reservationStatus, startDate, endDate, pageable);
    }
    // 상태만
    else if (reservationStatus != null) {
      return jpaReservationRepository.findByUserIdAndStatusOrderByReservationDateDesc(userId, reservationStatus, pageable);
    }
    // 날짜 범위만
    else if (startDate != null && endDate != null) {
      return jpaReservationRepository.findByUserIdAndReservationDateBetweenOrderByReservationDateDesc(
          userId, startDate, endDate, pageable);
    }
    // 스튜디오만
    else if (studioId != null) {
      return jpaReservationRepository.findByUserIdAndStudioIdOrderByReservationDateDesc(userId, studioId, pageable);
    }
    // 필터 없음
    else {
      return jpaReservationRepository.findByUserIdOrderByReservationDateDesc(userId, pageable);
    }
  }

  /**
   * Reservation 엔티티를 UserReservationResponse DTO로 변환
   * - 사용자별 예약 목록 조회 응답용
   * - 스튜디오명, 예약날짜/시간, 상태, 금액 정보 포함
   * @param reservation 예약 엔티티
   * @return 사용자 예약 응답 DTO
   */
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

  /**
   * 예약 취소 요청 (도메인 서비스 활용)
   * - 본인 예약 확인
   * - 시스템 설정 기반 취소 가능 시간 검증
   * - 취소 승인 대기 상태로 변경
   * @param id 예약 ID
   * @param request 취소 요청 정보 (사용자 ID, 취소 사유)
   * @return 취소 처리 결과
   */
  public ReservationCancelResponse cancelReservation(Long id, ReservationCancelRequest request) {
    // 1. 예약 조회 및 존재 확인
    Reservation reservation = jpaReservationRepository.findById(id)
        .orElseThrow(()-> new IllegalArgumentException("예약 Id를 찾을 수 없습니다."));

    // 2. 본인 예약 확인
    if (!reservation.getUser().getId().equals(request.userId())){
      throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
    }

    // 3. 취소 가능 시간 검증 (도메인 서비스 활용)
    LocalDateTime reservationDateTime = reservation.getReservationDate().atTime(reservation.getStartTime());
    if (!reservationDomainService.isWithinCancellationPeriod(reservationDateTime)) {
      int cancelHours = settingUtils.getIntegerSetting("reservation.cancel.hours", 24);
      throw new IllegalStateException("예약 시작 " + cancelHours + "시간 전까지만 취소 가능합니다.");
    }

    // 4. 취소 처리
    reservation.cancelWithoutValidation(request.reason());

    // 5. 저장
    Reservation saved = jpaReservationRepository.save(reservation);

    // 6. 응답 생성
    return new ReservationCancelResponse(
        saved.getId(),
        saved.getStatus(),
        LocalDateTime.now()
    );
  }

  /**
   * 결제 완료 시 예약 확정 처리
   * @param reservationId 예약 ID
   */
  @Transactional
  public void confirmReservationPayment(Long reservationId) {
    Reservation reservation = jpaReservationRepository.findById(reservationId)
        .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

    if (reservation.getStatus() != ReservationStatus.PENDING) {
      throw new IllegalStateException("예약 상태가 PENDING이 아닙니다.");
    }

    reservation.confirm();
    jpaReservationRepository.save(reservation);
  }
}