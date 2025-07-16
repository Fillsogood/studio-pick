package org.example.studiopick.application.workshop.dto;


import java.time.LocalDateTime;

public record WorkShopApplicationDetailResponse(
    Long workShopId,
    String title,
    String status,
    LocalDateTime createdAt,
    String message
) {}