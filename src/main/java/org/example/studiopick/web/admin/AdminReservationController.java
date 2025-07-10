package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminReservationService;
import org.example.studiopick.application.admin.dto.reservation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 관리자 예약 관리 API
 */
@Tag(name = "Admin Reservation", description = "관리자 예약 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    /**
     * 전체 예약 목록 조회 (필터링, 검색, 페이징)
     */
    @Operation(summary = "전체 예약 목록 조회", description = "필터링, 검색어, 페이징을 지원하는 예약 목록 조회")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @GetMapping
    public ResponseEntity<org.example.studiopick.common.dto.ApiResponse<AdminReservationListResponse>> getAllReservations(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) Long studioId,
        @RequestParam(required = false) String searchKeyword
    ) {
        log.info("전체 예약 목록 조회 요청: page={}, size={}, status={}, startDate={}, endDate={}, userId={}, studioId={}, searchKeyword={}",
            page, size, status, startDate, endDate, userId, studioId, searchKeyword);

        // ✅ LocalDate를 String으로 변환하여 Service 호출
        String startDateStr = startDate != null ? startDate.toString() : null;
        String endDateStr = endDate != null ? endDate.toString() : null;

        AdminReservationListResponse response = adminReservationService.getAllReservations(
            page, size, status, startDateStr, endDateStr, userId, studioId, searchKeyword);

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "예약 목록을 조회했습니다."));
    }

    /**
     * 예약 상세 조회
     */
    @Operation(summary = "예약 상세 조회", description = "특정 예약의 상세 정보 조회")
    @GetMapping("/{reservationId}")
    public ResponseEntity<org.example.studiopick.common.dto.ApiResponse<AdminReservationDetailResponse>> getReservationDetail(
        @PathVariable Long reservationId
    ) {
        log.info("예약 상세 조회 요청: reservationId={}", reservationId);

        AdminReservationDetailResponse response = adminReservationService.getReservationDetail(reservationId);

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "예약 상세 정보를 조회했습니다."));
    }

    /**
     * 예약 상태 변경 (관리자 권한)
     */
    @Operation(summary = "예약 상태 변경", description = "관리자가 예약 상태를 변경합니다")
    @PatchMapping("/{reservationId}/status")
    public ResponseEntity<org.example.studiopick.common.dto.ApiResponse<AdminReservationStatusResponse>> changeReservationStatus(
        @PathVariable Long reservationId,
        @Valid @RequestBody AdminReservationStatusCommand command
    ) {
        log.info("예약 상태 변경 요청: reservationId={}, status={}, reason={}",
            reservationId, command.status(), command.reason());

        AdminReservationStatusResponse response = adminReservationService.changeReservationStatus(
            reservationId, command);

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "예약 상태가 변경되었습니다."));
    }

    /**
     * 예약 통계 조회
     */
    @Operation(summary = "예약 통계 조회", description = "전체 예약 통계 정보 조회")
    @GetMapping("/stats")
    public ResponseEntity<org.example.studiopick.common.dto.ApiResponse<AdminReservationStatsResponse>> getReservationStats() {
        log.info("예약 통계 조회 요청");

        AdminReservationStatsResponse response = adminReservationService.getReservationStats();

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "예약 통계를 조회했습니다."));
    }

    /**
     * 사용자별 예약 내역 조회 (관리자용)
     */
    @Operation(summary = "사용자별 예약 내역", description = "특정 사용자의 모든 예약 내역 조회")
    @GetMapping("/users/{userId}")
    public ResponseEntity<org.example.studiopick.common.dto.ApiResponse<AdminReservationListResponse>> getUserReservations(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String status
    ) {
        log.info("사용자별 예약 내역 조회 요청: userId={}, page={}, size={}, status={}",
            userId, page, size, status);

        AdminReservationListResponse response = adminReservationService.getUserReservations(
            userId, page, size, status);

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "사용자 예약 내역을 조회했습니다."));
    }

    /**
     * 스튜디오별 예약 내역 조회 (관리자용)
     */
    @Operation(summary = "스튜디오별 예약 내역", description = "특정 스튜디오의 모든 예약 내역 조회")
    @GetMapping("/studios/{studioId}")
    public ResponseEntity<org.example.studiopick.common.dto.ApiResponse<AdminReservationListResponse>> getStudioReservations(
        @PathVariable Long studioId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String status
    ) {
        log.info("스튜디오별 예약 내역 조회 요청: studioId={}, page={}, size={}, status={}",
            studioId, page, size, status);

        AdminReservationListResponse response = adminReservationService.getStudioReservations(
            studioId, page, size, status);

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "스튜디오 예약 내역을 조회했습니다."));
    }

    /**
     * 오늘 예약 목록 조회 (편의 기능)
     */
    @Operation(summary = "오늘 예약 목록", description = "오늘 날짜의 예약 목록 조회")
    @GetMapping("/today")
    public ResponseEntity<org.example.studiopick.common.dto.ApiResponse<AdminReservationListResponse>> getTodayReservations(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String status
    ) {
        log.info("오늘 예약 목록 조회 요청: page={}, size={}, status={}", page, size, status);

        LocalDate today = LocalDate.now();
        AdminReservationListResponse response = adminReservationService.getAllReservations(
            page, size, status, today.toString(), today.toString(), null, null, null);

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "오늘 예약 목록을 조회했습니다."));
    }

    /**
     * 이번 주 예약 목록 조회 (편의 기능)
     */
    @Operation(summary = "이번 주 예약 목록", description = "이번 주 예약 목록 조회")
    @GetMapping("/this-week")
    public ResponseEntity<org.example.studiopick.common.dto.ApiResponse<AdminReservationListResponse>> getThisWeekReservations(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String status
    ) {
        log.info("이번 주 예약 목록 조회 요청: page={}, size={}, status={}", page, size, status);

        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = now.with(java.time.DayOfWeek.SUNDAY);

        AdminReservationListResponse response = adminReservationService.getAllReservations(
            page, size, status, startOfWeek.toString(), endOfWeek.toString(), null, null, null);

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "이번 주 예약 목록을 조회했습니다."));
    }
}
