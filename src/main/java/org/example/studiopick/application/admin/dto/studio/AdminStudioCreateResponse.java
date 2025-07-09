package org.example.studiopick.application.admin.dto.studio;

import java.time.LocalDateTime;

public record AdminStudioCreateResponse(
    Long studioId,
    String studioName,
    String ownerEmail,
    String status,
    LocalDateTime createdAt
) {}