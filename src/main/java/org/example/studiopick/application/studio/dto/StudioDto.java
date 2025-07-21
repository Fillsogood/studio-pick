package org.example.studiopick.application.studio.dto;

import org.example.studiopick.domain.common.enums.StudioStatus;

public record StudioDto(
    Long id,
    String name,
    String location,
    Long hourlyBaseRate,
    double averageRating,
    int reviewCount,
    String thumbnailImage,
    StudioStatus status
) {}
