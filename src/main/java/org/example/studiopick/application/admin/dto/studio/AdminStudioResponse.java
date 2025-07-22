package org.example.studiopick.application.admin.dto.studio;

import java.time.LocalDateTime;

public record AdminStudioResponse(
    Long id,
    String name,
    String ownerName,
    String ownerEmail,
    String phone,
    String Location,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
