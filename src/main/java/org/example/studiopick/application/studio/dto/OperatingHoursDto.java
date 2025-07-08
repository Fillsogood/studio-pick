package org.example.studiopick.application.studio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.studiopick.domain.common.enums.Weekday;
import java.time.LocalTime;

public record OperatingHoursDto(
    Weekday weekday,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime openTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime closeTime
) {}
