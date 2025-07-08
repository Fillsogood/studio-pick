package org.example.studiopick.application.studio.dto;

import java.util.List;

public record AvailableStudiosResponse(
    List<StudioAvailableDto> availableStudios
) {}
