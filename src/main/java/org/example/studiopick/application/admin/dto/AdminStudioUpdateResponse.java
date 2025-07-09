package org.example.studiopick.application.admin.dto;

import java.time.LocalDateTime;

public record AdminStudioUpdateResponse(
    Long studioId,
    String studioName,
    String ownerName,
    LocalDateTime updatedAt
) {}