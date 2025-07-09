package org.example.studiopick.application.admin.dto.reservation;

import java.time.LocalDateTime;

public record AdminReservationStatusResponse(
    Long reservationId,
    String userName,
    String studioName,
    String oldStatus,
    String newStatus,
    String reason,
    LocalDateTime changedAt
) {}