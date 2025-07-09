package org.example.studiopick.web.admin;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.ReservationRefundApproveService;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class ReservationRefundApproveController {

    private final ReservationRefundApproveService refundApproveService;

    @PostMapping("/{reservationId}/approve-refund")
    public ResponseEntity<ApiResponse<ReservationResponse>> approveRefund(@PathVariable Long reservationId) {
        ReservationResponse response = refundApproveService.approveRefund(reservationId);

        return ResponseEntity.ok(new ApiResponse<>(true, response, "환불이 승인되었습니다."));
    }
}
