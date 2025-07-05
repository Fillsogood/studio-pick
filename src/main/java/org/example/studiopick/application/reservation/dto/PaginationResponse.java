package org.example.studiopick.application.reservation.dto;

public record PaginationResponse(
    int page,
    long total
) {}