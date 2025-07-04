package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.ReservationService;
import org.example.studiopick.application.reservation.dto.AvailableTimesResponse;
import org.example.studiopick.application.reservation.dto.ReservationCreateCommand;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/studios/{studioId}/reservations")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  /**
   * 예약 생성
   */
  @PostMapping
  public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
      @PathVariable Long studioId,
      @RequestBody ReservationCreateCommand command
  ) {
    ReservationResponse response = reservationService.create(studioId, command);

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
      @PathVariable Long studioId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    AvailableTimesResponse response = reservationService.getAvailableTimes(studioId, date);
    return new ApiResponse<>(true, response, null);
  }
}
