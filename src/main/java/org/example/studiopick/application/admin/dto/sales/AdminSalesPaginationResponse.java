package org.example.studiopick.application.admin.dto.sales;

public record AdminSalesPaginationResponse(
    int currentPage,
    long totalElements,
    int totalPages
) {}
