package org.example.studiopick.application.classes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ClassListDto(
    Long id,
    String title,
    String description,
    BigDecimal price,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String studioName,
    int maxParticipants,
    int currentParticipants
) {}
