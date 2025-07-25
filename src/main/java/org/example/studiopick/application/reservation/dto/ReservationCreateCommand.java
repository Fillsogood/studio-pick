package org.example.studiopick.application.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationCreateCommand(
    Long studioId,
    Long workshopId,
    Long userId,
    LocalDate reservationDate,
    LocalTime startTime,
    LocalTime endTime,
    Short peopleCount
) {}