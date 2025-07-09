package org.example.studiopick.application.admin.dto.studio;

public record AdminPaginationResponse(
    int currentPage,
    long totalElements,
    int totalPages
) {}
