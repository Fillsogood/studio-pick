package org.example.studiopick.application.studio.dto;

import java.util.List;

public record StudioDetailDto(
    Long id,
    String name,
    String description,
    String phone,
    String location,
    List<String> images,
    PricingDto pricing,
    List<OperatingHoursDto> operatingHours,
    List<String> facilities
) {}
