package org.example.studiopick.application.studio.dto;

public record StudioImageUpdateDto(
    Long imageId,
    Integer displayOrder,
    boolean isThumbnail
) {}
