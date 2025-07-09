package org.example.studiopick.application.admin.dto.user;

import java.time.LocalDateTime;

public record AdminUserDetailResponse(
    Long id,
    String name,
    String email,
    String phone,
    String nickname,
    String role,
    String status,
    Boolean emailVerified,
    Boolean isStudioOwner,
    Short loginFailCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}