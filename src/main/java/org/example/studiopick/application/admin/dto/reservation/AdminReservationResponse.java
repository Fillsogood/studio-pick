package org.example.studiopick.application.admin.dto.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReservationResponse(
    Long id,
    String userName,
    String userEmail,
    String studioName,
    LocalDate reservationDate,
    LocalTime startTime,
    LocalTime endTime,
    Short peopleCount,
    Long totalAmount,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
