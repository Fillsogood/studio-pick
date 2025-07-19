package org.example.studiopick.application.studio.dto;

public record StudioListDto(
    Long id,
    String name,
    String location,
    Long hourlyBaseRate,
    double averageRating,
    int reviewCount,
    String thumbnailImage
) {}
