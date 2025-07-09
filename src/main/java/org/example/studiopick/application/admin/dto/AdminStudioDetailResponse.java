package org.example.studiopick.application.admin.dto;

import java.time.LocalDateTime;

public record AdminStudioDetailResponse(
    Long id,
    String name,
    String description,
    String address,
    String phone,
    Long hourlyBaseRate,
    Long perPersonRate,
    String status,
    AdminStudioOwnerInfo owner,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}