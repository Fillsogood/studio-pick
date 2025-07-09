package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.reservation.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReservationService {

  private final JpaReservationRepository reservationRepository;
  private final PaginationValidator paginationValidator;
  private final SystemSettingUtils settingUtils;

  /**
   * 전체 예약 목록 조회 (페이징, 필터링)
   */
  public AdminReservationListResponse getAllReservations(
      int page, Integer size, String status, String startDate, String endDate,
      Long userId, Long studioId) {

    // 입력값 검증
    int pageSize = size != null ? size : settingUtils.getIntegerSetting("pagination.default.size", 10);
    paginationValidator.validatePaginationParameters(page, pageSize);
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Reservation> reservationsPage;

    // 날짜 파싱
    LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
    LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
    ReservationStatus reservationStatus = status != null ? parseReservationStatus(status) : null;

    // 복합 조건 검색
    reservationsPage = getFilteredReservations(
        reservationStatus, start, end, userId, studioId, pageable);

    List<AdminReservationResponse> reservations = reservationsPage.getContent()
        .stream()
        .map(this::toAdminReservationResponse)
        .toList();

    return new AdminReservationListResponse(
        reservations,
        new AdminReservationPaginationResponse(page, reservationsPage.getTotalElements(), reservationsPage.getTotalPages())
    );
  }

  /**
   * 예약 상세 조회
   */
  public AdminReservationDetailResponse getReservationDetail(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

    return toAdminReservationDetailResponse(reservation);
  }

  /**
   * 예약 상태 변경 (관리자 권한)
   */
  @Transactional
  public AdminReservationStatusResponse changeReservationStatus(
      Long reservationId, AdminReservationStatusCommand command) {

    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

    ReservationStatus oldStatus = reservation.getStatus();
    ReservationStatus newStatus = parseReservationStatus(command.status());

    // 상태 변경 유효성 검증
    validateStatusChange(oldStatus, newStatus);

    try {
      // 상태별 처리
      switch (newStatus) {
        case CONFIRMED -> reservation.confirm();
        case CANCELLED -> {
          reservation.changeStatus(ReservationStatus.CANCELLED);
          reservation.updateCancelInfo(command.reason());
        }
        case REFUNDED -> reservation.changeStatus(ReservationStatus.REFUNDED);
        case COMPLETED -> reservation.complete();
        default -> reservation.changeStatus(newStatus);
      }

      reservationRepository.save(reservation);

      log.info("예약 상태 변경 완료: reservationId={}, {} -> {}, reason={}",
          reservationId, oldStatus, newStatus, command.reason());

      return new AdminReservationStatusResponse(
          reservation.getId(),
          reservation.getUser().getName(),
          reservation.getStudio().getName(),
          oldStatus.getValue(),
          newStatus.getValue(),
          command.reason(),
          LocalDateTime.now()
      );

    } catch (Exception e) {
      log.error("예약 상태 변경 실패: reservationId={}, error={}", reservationId, e.getMessage());
      throw new RuntimeException("예약 상태 변경에 실패했습니다.", e);
    }
  }

  /**
   * 예약 통계 조회
   */
  public AdminReservationStatsResponse getReservationStats() {
    long totalReservations = reservationRepository.count();
    long pendingReservations = reservationRepository.countByStatus(ReservationStatus.PENDING);
    long confirmedReservations = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
    long cancelledReservations = reservationRepository.countByStatus(ReservationStatus.CANCELLED);
    long completedReservations = reservationRepository.countByStatus(ReservationStatus.COMPLETED);
    long refundedReservations = reservationRepository.countByStatus(ReservationStatus.REFUNDED);

    // 오늘 예약 수
    long todayReservations = reservationRepository.countByReservationDate(LocalDate.now());

    return new AdminReservationStatsResponse(
        totalReservations,
        pendingReservations,
        confirmedReservations,
        cancelledReservations,
        completedReservations,
        refundedReservations,
        todayReservations
    );
  }

  /**
   * 사용자별 예약 내역 조회 (관리자용)
   */
  public AdminReservationListResponse getUserReservations(
      Long userId, int page, int size, String status) {

    paginationValidator.validatePaginationParameters(page, size);

    Pageable pageable = PageRequest.of(page - 1, size);
    Page<Reservation> reservationsPage;

    if (status != null) {
      ReservationStatus reservationStatus = parseReservationStatus(status);
      reservationsPage = reservationRepository.findByUserIdAndStatusOrderByReservationDateDesc(
          userId, reservationStatus, pageable);
    } else {
      reservationsPage = reservationRepository.findByUserIdOrderByReservationDateDesc(
          userId, pageable);
    }

    List<AdminReservationResponse> reservations = reservationsPage.getContent()
        .stream()
        .map(this::toAdminReservationResponse)
        .toList();

    return new AdminReservationListResponse(
        reservations,
        new AdminReservationPaginationResponse(page, reservationsPage.getTotalElements(), reservationsPage.getTotalPages())
    );
  }

  /**
   * 스튜디오별 예약 내역 조회 (관리자용)
   */
  public AdminReservationListResponse getStudioReservations(
      Long studioId, int page, int size, String status) {

    paginationValidator.validatePaginationParameters(page, size);

    Pageable pageable = PageRequest.of(page - 1, size);
    Page<Reservation> reservationsPage;

    if (status != null) {
      ReservationStatus reservationStatus = parseReservationStatus(status);
      reservationsPage = reservationRepository.findByStudioIdAndStatusOrderByReservationDateDesc(
          studioId, reservationStatus, pageable);
    } else {
      reservationsPage = reservationRepository.findByStudioIdOrderByReservationDateDesc(
          studioId, pageable);
    }

    List<AdminReservationResponse> reservations = reservationsPage.getContent()
        .stream()
        .map(this::toAdminReservationResponse)
        .toList();

    return new AdminReservationListResponse(
        reservations,
        new AdminReservationPaginationResponse(page, reservationsPage.getTotalElements(), reservationsPage.getTotalPages())
    );
  }

  // Private helper methods

  private Page<Reservation> getFilteredReservations(
      ReservationStatus status, LocalDate startDate, LocalDate endDate,
      Long userId, Long studioId, Pageable pageable) {

    // 모든 조건이 있는 경우
    if (status != null && startDate != null && endDate != null && userId != null && studioId != null) {
      return reservationRepository.findByStatusAndReservationDateBetweenAndUserIdAndStudioIdOrderByReservationDateDesc(
          status, startDate, endDate, userId, studioId, pageable);
    }
    // 상태 + 날짜 범위 + 사용자
    else if (status != null && startDate != null && endDate != null && userId != null) {
      return reservationRepository.findByStatusAndReservationDateBetweenAndUserIdOrderByReservationDateDesc(
          status, startDate, endDate, userId, pageable);
    }
    // 상태 + 날짜 범위 + 스튜디오
    else if (status != null && startDate != null && endDate != null && studioId != null) {
      return reservationRepository.findByStatusAndReservationDateBetweenAndStudioIdOrderByReservationDateDesc(
          status, startDate, endDate, studioId, pageable);
    }
    // 상태 + 날짜 범위
    else if (status != null && startDate != null && endDate != null) {
      return reservationRepository.findByStatusAndReservationDateBetweenOrderByReservationDateDesc(
          status, startDate, endDate, pageable);
    }
    // 날짜 범위만
    else if (startDate != null && endDate != null) {
      return reservationRepository.findByReservationDateBetweenOrderByReservationDateDesc(
          startDate, endDate, pageable);
    }
    // 상태만
    else if (status != null) {
      return reservationRepository.findByStatusOrderByReservationDateDesc(status, pageable);
    }
    // 사용자만
    else if (userId != null) {
      return reservationRepository.findByUserIdOrderByReservationDateDesc(userId, pageable);
    }
    // 스튜디오만
    else if (studioId != null) {
      return reservationRepository.findByStudioIdOrderByReservationDateDesc(studioId, pageable);
    }
    // 전체
    else {
      return reservationRepository.findAllByOrderByReservationDateDesc(pageable);
    }
  }

  private void validateStatusChange(ReservationStatus oldStatus, ReservationStatus newStatus) {
    // 비즈니스 룰에 따른 상태 변경 유효성 검증
    if (oldStatus == newStatus) {
      throw new IllegalArgumentException("동일한 상태로 변경할 수 없습니다.");
    }

    // 완료된 예약은 변경 불가
    if (oldStatus == ReservationStatus.COMPLETED) {
      throw new IllegalArgumentException("완료된 예약은 상태를 변경할 수 없습니다.");
    }

    // 환불된 예약은 변경 불가
    if (oldStatus == ReservationStatus.REFUNDED) {
      throw new IllegalArgumentException("환불된 예약은 상태를 변경할 수 없습니다.");
    }
  }

  private ReservationStatus parseReservationStatus(String status) {
    try {
      return ReservationStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("잘못된 예약 상태입니다: " + status);
    }
  }

  private AdminReservationResponse toAdminReservationResponse(Reservation reservation) {
    return new AdminReservationResponse(
        reservation.getId(),
        reservation.getUser().getName(),
        reservation.getUser().getEmail(),
        reservation.getStudio().getName(),
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getPeopleCount(),
        reservation.getTotalAmount(),
        reservation.getStatus().getValue(),
        reservation.getCreatedAt(),
        reservation.getUpdatedAt()
    );
  }

  private AdminReservationDetailResponse toAdminReservationDetailResponse(Reservation reservation) {
    return new AdminReservationDetailResponse(
        reservation.getId(),
        new AdminReservationUserInfo(
            reservation.getUser().getId(),
            reservation.getUser().getName(),
            reservation.getUser().getEmail(),
            reservation.getUser().getPhone()
        ),
        new AdminReservationStudioInfo(
            reservation.getStudio().getId(),
            reservation.getStudio().getName(),
            reservation.getStudio().getPhone(),
            reservation.getStudio().getLocation()
        ),
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getPeopleCount(),
        reservation.getTotalAmount(),
        reservation.getStatus().getValue(),
        reservation.getCancelledReason(),
        reservation.getCancelledAt(),
        reservation.getCreatedAt(),
        reservation.getUpdatedAt()
    );
  }
}