package org.example.studiopick.application.admin.dto.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReservationDetailResponse(
    Long id,
    AdminReservationUserInfo user,
    AdminReservationStudioInfo studio,
    LocalDate reservationDate,
    LocalTime startTime,
    LocalTime endTime,
    Short peopleCount,
    Long totalAmount,
    String status,
    String cancelReason,
    LocalDateTime cancelledAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}