package org.example.studiopick.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.reservation.ReservationService;
import org.example.studiopick.application.reservation.dto.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 예약 관리 API
 */
@Tag(name = "Reservation", description = "예약 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약 생성
     */
    @Operation(summary = "예약 생성", description = "새로운 스튜디오 예약을 생성합니다")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "예약 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "예약 시간 중복"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
        @Valid @RequestBody ReservationCreateCommand command,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // 토큰에서 직접 사용자 ID 추출
        Long userId = userPrincipal.getUserId();
        log.info("예약 생성 요청: userId={}, studioId={}, date={}, startTime={}, endTime={}",
            userId, command.studioId(), command.reservationDate(), 
            command.startTime(), command.endTime());

        ReservationResponse response = reservationService.create(command.studioId(), command);

        log.info("예약 생성 완료: reservationId={}", response.id());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, response, "예약이 신청되었습니다."));
    }

    /**
     * 예약 가능 시간 조회
     */
    @Operation(summary = "예약 가능 시간 조회", description = "특정 날짜의 스튜디오 예약 가능 시간을 조회합니다")
    @GetMapping("/available-times")
    @PreAuthorize("permitAll()")  // 누구나 조회 가능
    public ResponseEntity<ApiResponse<AvailableTimesResponse>> getAvailableTimes(
        @RequestParam Long studioId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("예약 가능 시간 조회 요청: studioId={}, date={}", studioId, date);

        AvailableTimesResponse response = reservationService.getAvailableTimes(studioId, date);
        
        return ResponseEntity.ok(new ApiResponse<>(true, response, "예약 가능 시간을 조회했습니다."));
    }

    /**
     * 내 예약 목록 조회
     */
    @Operation(summary = "내 예약 목록 조회", description = "본인의 예약 목록을 조회합니다 (필터링 지원)")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<UserReservationListResponse>> getMyReservations(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long studioId
    ) {
        // 토큰에서 직접 사용자 ID 추출
        Long userId = userPrincipal.getUserId();
        log.info("사용자 예약 목록 조회 요청: userId={}, page={}, size={}, status={}, startDate={}, endDate={}, studioId={}",
            userId, page, size, status, startDate, endDate, studioId);

        UserReservationListResponse response = reservationService
            .getUserReservations(userId, page, size, status, startDate, endDate, studioId);

        return ResponseEntity.ok(new ApiResponse<>(true, response, "예약 목록을 조회했습니다."));
    }

    /**
     * 예약 상세 조회
     */
    @Operation(summary = "예약 상세 조회", description = "특정 예약의 상세 정보를 조회합니다")
    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<UserReservationDetailResponse>> getReservationDetail(
        @PathVariable Long reservationId,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // 토큰에서 직접 사용자 ID 추출
        Long userId = userPrincipal.getUserId();
        log.info("예약 상세 조회 요청: reservationId={}, userId={}", reservationId, userId);

        UserReservationDetailResponse response = reservationService.getReservationDetail(reservationId, userId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, response, "예약 상세 정보를 조회했습니다."));
    }

    /**
     * 예약 취소
     */
    @Operation(summary = "예약 취소", description = "예약을 취소합니다")
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<ReservationCancelResponse>> cancelReservation(
        @PathVariable Long reservationId,
        @Valid @RequestBody ReservationCancelCommand command,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // 토큰에서 직접 사용자 ID 추출
        Long userId = userPrincipal.getUserId();
        log.info("예약 취소 요청: reservationId={}, userId={}, reason={}", 
            reservationId, userId, command.cancelReason());

        // ReservationCancelRequest 생성
        ReservationCancelRequest request = new ReservationCancelRequest(userId, command.cancelReason());
        ReservationCancelResponse response = reservationService.cancelReservation(reservationId, request);
        
        log.info("예약 취소 완료: reservationId={}", reservationId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, response, "예약이 취소되었습니다."));
    }
}
