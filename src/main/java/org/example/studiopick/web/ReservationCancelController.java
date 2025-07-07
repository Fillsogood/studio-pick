package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.ReservationCancelService;
import org.example.studiopick.application.reservation.dto.CancelReservationCommand;
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
public class ReservationCancelController {

    private final ReservationCancelService reservationCancelService;

    //예약취소 api
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @RequestBody CancelReservationCommand command
    ) {
        System.out.println("===> CancelReservationCommand: " + command); // 로그 찍기

        ReservationResponse response = reservationCancelService.cancelReservation(command);

        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>(
                true,
                response,
                "예약이 취소 되었습니다."
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
