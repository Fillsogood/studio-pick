package org.example.studiopick.application.studio.dto;

import java.util.List;

public record StudioSearchResponse(
    List<StudioSearchDto> studios,
    int totalCount
) {}
