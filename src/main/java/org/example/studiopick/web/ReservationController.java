package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.ReservationService;
import org.example.studiopick.application.reservation.dto.AvailableTimesResponse;
import org.example.studiopick.application.reservation.dto.ReservationCreateCommand;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.application.reservation.dto.UserReservationListResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  /**
   * 예약 생성
   */
  @PostMapping
  public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
      @RequestBody ReservationCreateCommand command
  ) {
    ReservationResponse response = reservationService.create(command.studioId(), command);

    ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "예약이 신청되었습니다."
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
  }

  /**
   * 예약 가능 시간 조회
   */
  @GetMapping("/available-times")
  public ApiResponse<AvailableTimesResponse> getAvailableTimes(
      @RequestParam Long studioId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    AvailableTimesResponse response = reservationService.getAvailableTimes(studioId, date);
    return new ApiResponse<>(true, response, null);
  }

  /**
   * 사용자별 예약 목록 조회
   */
  @GetMapping
  public ApiResponse<UserReservationListResponse> getReservations(
      @RequestParam Long userId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    UserReservationListResponse response = reservationService
        .getUserReservations(userId, page, size);

    return new ApiResponse<>(true, response, null);
  }
}