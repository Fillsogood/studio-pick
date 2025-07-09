package org.example.studiopick.application.admin.dto.user;

import java.time.LocalDateTime;

public record AdminUserResponse(
    Long id,
    String name,
    String email,
    String phone,
    String nickname,
    String role,
    String status,
    Boolean emailVerified,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}