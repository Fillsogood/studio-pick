package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.ReservationService;
import org.example.studiopick.application.reservation.dto.ReservationCreateCommand;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
  private final ReservationService reservationService;

  @PostMapping
  public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
      @RequestBody ReservationCreateCommand command) {

    ReservationResponse response = reservationService.create(command);

    ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "예약이 신청되었습니다"
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
  }
}