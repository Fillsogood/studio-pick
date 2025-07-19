package org.example.studiopick.application.studio.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudioUpdateRequest(
    String description,
    String phone,
    Long hourlyBaseRate,
    BigDecimal weekendPrice,
    Integer maxPeople,
    Long perPersonRate,
    List<OperatingHoursDto> operatingHours,
    Integer size,
    String facilities,
    String rules,
    String thumbnailImage,
    List<String> imageUrls
) {}
