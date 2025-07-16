package org.example.studiopick.application.workshop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record WorkShopListDto(
    Long id,
    String title,
    String description,
    BigDecimal price,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    int maxParticipants,
    int currentParticipants
) {}
