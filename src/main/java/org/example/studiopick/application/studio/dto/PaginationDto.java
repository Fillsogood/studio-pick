package org.example.studiopick.application.studio.dto;

public record PaginationDto(
    int page,
    int limit,
    long total,
    int totalPages
) {}
