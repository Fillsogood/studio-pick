package org.example.studiopick.application.reservation.dto;

import org.example.studiopick.domain.common.enums.ReservationStatus;

public record ReservationResponse(
    Long reservationId,
    Long totalAmount,
    ReservationStatus status,
    Long refundAmount
    ) {
    // 환불 없는 경우 생성자
    public ReservationResponse(Long reservationId, Long totalAmount, ReservationStatus status) {
        this(reservationId, totalAmount, status, null);
    }
}