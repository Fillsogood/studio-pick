package org.example.studiopick.application.admin.dto.user;

import java.time.LocalDateTime;

public record AdminUserUpdateResponse(
    Long userId,
    String name,
    String email,
    LocalDateTime updatedAt
) {}
