package org.example.studiopick.common.dto.artwork;

import java.time.LocalDateTime;

public record StudioArtworkSummaryDto(
        Long id,
        String title,
        String imageUrl,
        boolean isPublic,
        LocalDateTime createdAt
) {}
