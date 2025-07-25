package org.example.studiopick.application.admin.dto.studio;

import java.time.LocalDateTime;

public record AdminStudioStatusResponse(
    Long studioId,
    String studioName,
    String oldStatus,
    String newStatus,
    String reason,
    LocalDateTime changedAt
) {}