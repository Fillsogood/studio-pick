package org.example.studiopick.application.workshop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record WorkShopApplicationRequest(
    String title,
    String description,
    BigDecimal price,
    LocalDate date,
    String instructor,
    LocalTime startTime,
    LocalTime endTime
) {}
