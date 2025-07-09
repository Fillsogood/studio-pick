package org.example.studiopick.application.admin.dto.user;

import java.time.LocalDateTime;

public record AdminUserStatusResponse(
    Long userId,
    String name,
    String oldStatus,
    String newStatus,
    String reason,
    LocalDateTime changedAt
) {}
