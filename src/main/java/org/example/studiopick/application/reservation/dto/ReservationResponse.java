package org.example.studiopick.application.reservation.dto;

import org.example.studiopick.domain.common.enums.ReservationStatus;

public record ReservationResponse(
    Long reservationId,
    Long totalAmount,
    ReservationStatus status,
    Long refundAmount
    ) {}