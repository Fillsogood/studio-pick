package org.example.studiopick.application.admin.dto.reservation;

public record AdminReservationPaginationResponse(
    int currentPage,
    long totalElements,
    int totalPages
) {}