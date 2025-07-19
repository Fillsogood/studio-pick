package org.example.studiopick.application.studio.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudioDetailDto(
    Long id,
    String name,
    String description,
    String phone,
    String location,
    Long hourlyBaseRate,
    BigDecimal weekendPrice,
    Long perPersonRate,
    Integer maxPeople,
    Integer size,
    String facilities,
    String rules,
    String thumbnailImage,
    List<String> imageUrls,
    List<OperatingHoursDto> operatingHours
) {}
