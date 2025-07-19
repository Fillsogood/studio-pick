package org.example.studiopick.application.studio.dto;

import java.util.List;

public record StudioListResponse(
    List<StudioListDto> content,
    PaginationDto pagination
) {}
