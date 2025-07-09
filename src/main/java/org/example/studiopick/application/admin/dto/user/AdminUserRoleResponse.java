package org.example.studiopick.application.admin.dto.user;

import java.time.LocalDateTime;

public record AdminUserRoleResponse(
    Long userId,
    String name,
    String oldRole,
    String newRole,
    String reason,
    LocalDateTime changedAt
) {}
