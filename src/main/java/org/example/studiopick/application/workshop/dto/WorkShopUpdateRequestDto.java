package org.example.studiopick.application.workshop.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkShopUpdateRequestDto {
    private String title;
    private String description;
    private BigDecimal price;
    private String date;
    private String instructor;
    private TimeRequest startTime;
    private TimeRequest endTime;
    private String thumbnailUrl;
    private List<String> imageUrls;
    private String address;

    // CreateCommand와 동일한 내부 타입
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRequest {
        private int hour;
        private int minute;
        private int second;
        private int nano;
    }
}
