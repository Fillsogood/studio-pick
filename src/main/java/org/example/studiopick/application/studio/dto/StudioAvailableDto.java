package org.example.studiopick.application.studio.dto;

public record StudioAvailableDto(
    Long id,
    String name,
    String location,
    String nextAvailableTime
) {}
