package org.example.studiopick.application.studio.dto;

import org.example.studiopick.domain.common.enums.StudioStatus;

public record StudioDto(
    Long id,
    String name,
    String description,
    String location,
    StudioStatus status
) {}
