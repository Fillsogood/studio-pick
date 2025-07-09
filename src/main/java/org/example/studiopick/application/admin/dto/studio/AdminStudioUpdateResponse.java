package org.example.studiopick.application.admin.dto.studio;

import java.time.LocalDateTime;

public record AdminStudioUpdateResponse(
    Long studioId,
    String studioName,
    String ownerName,
    LocalDateTime updatedAt
) {}