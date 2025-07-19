package org.example.studiopick.application.studio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.studiopick.domain.common.enums.Weekday;
import org.example.studiopick.domain.studio.StudioOperatingHours;

import java.time.LocalTime;

public record OperatingHoursDto(
    Weekday weekday,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime openTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime closeTime
) {
    public StudioOperatingHours toEntity() {
        return StudioOperatingHours.builder()
            .weekday(weekday)
            .openTime(openTime)
            .closeTime(closeTime)
            .build();
    }

    public static OperatingHoursDto fromEntity(StudioOperatingHours entity) {
        return new OperatingHoursDto(
            entity.getWeekday(),
            entity.getOpenTime(),
            entity.getCloseTime()
        );
    }
}
