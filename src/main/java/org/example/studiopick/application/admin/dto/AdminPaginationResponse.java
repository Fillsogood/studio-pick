package org.example.studiopick.application.admin.dto;

public record AdminPaginationResponse(
    int currentPage,
    long totalElements,
    int totalPages
) {}
