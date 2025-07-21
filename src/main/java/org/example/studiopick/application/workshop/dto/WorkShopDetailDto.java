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
        int startHour,
        int startMinute,
        int endHour,
        int endMinute,
        String instructor,
        int maxParticipants,
        List<String> materials,
        String address,
        String thumbnailUrl,
        List<String> imageUrls
) {
    public WorkShopDetailDto(Long id, String title, String description, BigDecimal price, LocalDate date,
                             LocalTime startTime, LocalTime endTime, String instructor, int maxParticipants,
                             List<String> materials, String address, String thumbnailUrl, List<String> imageUrls) {
        this(id, title, description, price, date,
                startTime != null ? startTime.getHour() : 0,
                startTime != null ? startTime.getMinute() : 0,
                endTime != null ? endTime.getHour() : 0,
                endTime != null ? endTime.getMinute() : 0,
                instructor, maxParticipants, materials, address, thumbnailUrl, imageUrls);
    }
}
