package org.example.studiopick.application.studio.dto;

public record PaginationDto(
    int currentPage,
    int pageSize,
    long totalElements,
    int totalPages
) {}
