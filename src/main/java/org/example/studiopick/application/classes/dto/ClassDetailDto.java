package org.example.studiopick.application.classes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ClassDetailDto(
    Long id,
    String title,
    String description,
    BigDecimal price,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String studioName,
    String studioLocation,
    String instructor,
    int maxParticipants,
    int currentParticipants,
    List<String> materials
) {}
