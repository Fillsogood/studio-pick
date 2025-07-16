package org.example.studiopick.application.workshop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record WorkShopDetailDto(
    Long id,
    String title,
    String description,
    BigDecimal price,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String instructor,
    int maxParticipants,
    List<String> materials
) {}
