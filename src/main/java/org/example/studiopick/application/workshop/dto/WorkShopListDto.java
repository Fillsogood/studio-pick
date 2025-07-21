package org.example.studiopick.application.workshop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record WorkShopListDto(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String instructor,
        String thumbnailUrl,
        List<String> imageUrls,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Double rating
) {}
