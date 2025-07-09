package org.example.studiopick.application.admin.dto.user;

import java.time.LocalDateTime;

public record AdminUserCreateResponse(
    Long userId,
    String name,
    String email,
    String role,
    String status,
    LocalDateTime createdAt
) {}
