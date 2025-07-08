package org.example.studiopick.application.reservation.dto;

import org.example.studiopick.domain.common.enums.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationCancelResponse(
    Long reservationId,
    ReservationStatus status,
    LocalDateTime cancelledAt
) {}