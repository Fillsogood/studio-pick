package org.example.studiopick.application.studio.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class StudioClassResponseDto {

    private Long classId;
    private String title;
    private String description;
    private BigDecimal price;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String instructor;
    private String status;
}
