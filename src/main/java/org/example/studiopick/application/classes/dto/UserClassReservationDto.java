package org.example.studiopick.application.classes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record UserClassReservationDto(
    Long id,
    String classTitle,
    String instructor,
    LocalDate date,
    LocalTime startTime,
    String studioName,
    int participants,
    BigDecimal amount,
    String status
) {}
