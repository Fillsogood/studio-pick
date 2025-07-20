package org.example.studiopick.application.workshop.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkShopApplicationRequest(
        String title,
        String description,
        BigDecimal price,
        String date,
        String instructor,
        TimeRequest startTime,
        TimeRequest endTime,
        String thumbnailUrl,
        List<String> imageUrls,
        String address
) {
    public record TimeRequest(int hour, int minute, int second, int nano) {}
}
