package org.example.studiopick.application.studio.dto;

public record StudioListDto(
    Long id,
    String name,
    String description,
    String location,
    double averageRating,
    int reviewCount
) {}
