package org.example.studiopick.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminReservationService;
import org.example.studiopick.application.admin.dto.reservation.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

  private final AdminReservationService adminReservationService;

  /**
   * 전체 예약 목록 조회
   * GET /api/admin/reservations?page=1&size=10&status=confirmed&startDate=2025-01-01&endDate=2025-01-31&userId=1&studioId=1
   */
  @GetMapping
  public ResponseEntity<ApiResponse<AdminReservationListResponse>> getAllReservations(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) Long studioId
  ) {
    log.info("전체 예약 목록 조회 요청: page={}, size={}, status={}, startDate={}, endDate={}, userId={}, studioId={}",
        page, size, status, startDate, endDate, userId, studioId);

    AdminReservationListResponse response = adminReservationService.getAllReservations(
        page, size, status, startDate, endDate, userId, studioId);

    ApiResponse<AdminReservationListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "예약 목록을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 예약 상세 조회
   * GET /api/admin/reservations/{reservationId}
   */
  @GetMapping("/{reservationId}")
  public ResponseEntity<ApiResponse<AdminReservationDetailResponse>> getReservationDetail(
      @PathVariable Long reservationId
  ) {
    log.info("예약 상세 조회 요청: reservationId={}", reservationId);

    AdminReservationDetailResponse response = adminReservationService.getReservationDetail(reservationId);

    ApiResponse<AdminReservationDetailResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "예약 상세 정보를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 예약 상태 변경 (관리자 권한)
   * PATCH /api/admin/reservations/{reservationId}/status
   */
  @PatchMapping("/{reservationId}/status")
  public ResponseEntity<ApiResponse<AdminReservationStatusResponse>> changeReservationStatus(
      @PathVariable Long reservationId,
      @Valid @RequestBody AdminReservationStatusCommand command
  ) {
    log.info("예약 상태 변경 요청: reservationId={}, status={}, reason={}",
        reservationId, command.status(), command.reason());

    AdminReservationStatusResponse response = adminReservationService.changeReservationStatus(
        reservationId, command);

    ApiResponse<AdminReservationStatusResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "예약 상태가 변경되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 예약 통계 조회
   * GET /api/admin/reservations/stats
   */
  @GetMapping("/stats")
  public ResponseEntity<ApiResponse<AdminReservationStatsResponse>> getReservationStats() {
    log.info("예약 통계 조회 요청");

    AdminReservationStatsResponse response = adminReservationService.getReservationStats();

    ApiResponse<AdminReservationStatsResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "예약 통계를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 사용자별 예약 내역 조회 (관리자용)
   * GET /api/admin/reservations/users/{userId}?page=1&size=10&status=confirmed
   */
  @GetMapping("/users/{userId}")
  public ResponseEntity<ApiResponse<AdminReservationListResponse>> getUserReservations(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status
  ) {
    log.info("사용자별 예약 내역 조회 요청: userId={}, page={}, size={}, status={}",
        userId, page, size, status);

    AdminReservationListResponse response = adminReservationService.getUserReservations(
        userId, page, size, status);

    ApiResponse<AdminReservationListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "사용자 예약 내역을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 스튜디오별 예약 내역 조회 (관리자용)
   * GET /api/admin/reservations/studios/{studioId}?page=1&size=10&status=confirmed
   */
  @GetMapping("/studios/{studioId}")
  public ResponseEntity<ApiResponse<AdminReservationListResponse>> getStudioReservations(
      @PathVariable Long studioId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status
  ) {
    log.info("스튜디오별 예약 내역 조회 요청: studioId={}, page={}, size={}, status={}",
        studioId, page, size, status);

    AdminReservationListResponse response = adminReservationService.getStudioReservations(
        studioId, page, size, status);

    ApiResponse<AdminReservationListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "스튜디오 예약 내역을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 오늘 예약 목록 조회 (빠른 접근용)
   * GET /api/admin/reservations/today?page=1&size=10&status=confirmed
   */
  @GetMapping("/today")
  public ResponseEntity<ApiResponse<AdminReservationListResponse>> getTodayReservations(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status
  ) {
    log.info("오늘 예약 목록 조회 요청: page={}, size={}, status={}", page, size, status);

    // 오늘 날짜로 조회
    String today = java.time.LocalDate.now().toString();
    AdminReservationListResponse response = adminReservationService.getAllReservations(
        page, size, status, today, today, null, null);

    ApiResponse<AdminReservationListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "오늘 예약 목록을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 이번 주 예약 목록 조회 (빠른 접근용)
   * GET /api/admin/reservations/this-week?page=1&size=10&status=confirmed
   */
  @GetMapping("/this-week")
  public ResponseEntity<ApiResponse<AdminReservationListResponse>> getThisWeekReservations(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status
  ) {
    log.info("이번 주 예약 목록 조회 요청: page={}, size={}, status={}", page, size, status);

    // 이번 주 월요일부터 일요일까지
    java.time.LocalDate now = java.time.LocalDate.now();
    java.time.LocalDate startOfWeek = now.with(java.time.DayOfWeek.MONDAY);
    java.time.LocalDate endOfWeek = now.with(java.time.DayOfWeek.SUNDAY);

    AdminReservationListResponse response = adminReservationService.getAllReservations(
        page, size, status, startOfWeek.toString(), endOfWeek.toString(), null, null);

    ApiResponse<AdminReservationListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "이번 주 예약 목록을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }
}