package org.example.studiopick.application.workshop.dto;

import org.example.studiopick.domain.workshop.WorkShop;
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
        String address,
        String thumbnailUrl,
        List<String> imageUrls
) {
    public WorkShopDetailDto(Long id, String title, String description, BigDecimal price, LocalDate date,
                             LocalTime startTime, LocalTime endTime, String instructor,
                             String address, String thumbnailUrl, List<String> imageUrls) {
        this(id, title, description, price, date,
                startTime != null ? startTime.getHour() : 0,
                startTime != null ? startTime.getMinute() : 0,
                endTime   != null ? endTime.getHour()   : 0,
                endTime   != null ? endTime.getMinute() : 0,
                instructor, address, thumbnailUrl, imageUrls);
    }

    /**
     * WorkShop 엔티티를 DTO로 변환하는 팩토리 메서드
     */
    public static WorkShopDetailDto of(WorkShop ws) {
        return new WorkShopDetailDto(
                ws.getId(),
                ws.getTitle(),
                ws.getDescription(),
                ws.getPrice(),
                ws.getDate(),
                ws.getStartTime(),
                ws.getEndTime(),
                ws.getInstructor(),
                ws.getAddress(),
                ws.getThumbnailUrl(),
                ws.getImageUrls()
        );
    }
}
