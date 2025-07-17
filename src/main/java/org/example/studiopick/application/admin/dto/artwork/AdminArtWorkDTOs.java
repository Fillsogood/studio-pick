package org.example.studiopick.application.admin.dto.artwork;

import org.example.studiopick.domain.common.enums.ArtworkStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminArtWorkDTOs {

  public record AdminArtWorkListResponse(
      List<AdminArtWorkSummaryDto> artworks,
      long totalElements
  ) {
    public record AdminArtWorkSummaryDto(
        Long id,
        String title,
        String ownerName,
        String status,
        LocalDateTime createdAt
    ) {}
  }

  public record AdminArtWorkPaginationResponse(int currentPage, long totalElements, int totalPages) {}

  public record AdminArtWorkDetailResponse(
      Long id,
      String title,
      String description,
      String imageUrl,
      String hashtags,
      String status,
      String shootingDate,
      String shootingLocation,
      int likeCount,
      int commentCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}

  public record AdminArtWorkStatusCommand(ArtworkStatus status, String reason) {}

  public record AdminArtWorkStatusResponse(
      Long artworkId,
      String oldStatus,
      String newStatus,
      String reason,
      LocalDateTime changedAt
  ) {}

  public record AdminArtWorkStatsResponse(
      long totalArtworks,
      long publicArtworks,
      long privateArtworks,
      long reportedArtworks,
      BigDecimal averageLikes
  ) {}

  public record AdminPopularArtWorkResponse(
      String period,
      List<PopularArtWork> artworks
  ) {
    public record PopularArtWork(
        Long id,
        String title,
        String ownerName,
        int likeCount,
        int commentCount
    ) {}
  }

  public record ReportedArtWorkDto(
      Long artworkId,
      String title,
      String ownerName,
      int totalReportCount,
      List<String> reportReasons,
      String status,
      LocalDateTime createdAt
  ) {}

  public record AdminArtWorkCommentDto(
      Long commentId,
      Long artworkId,
      String commenterName,
      String comment,
      LocalDateTime createdAt
  ) {}

  public record AdminArtWorkCommentCreateCommand(
      Long artworkId,
      String comment
  ) {}

  public record AdminArtWorkCreateCommand(
      Long userId,
      String title,
      String description,
      String imageUrl,
      String hashtags,
      String shootingDate,
      String shootingLocation,
      Boolean isPublic,
      ArtworkStatus status
  ) {}
}
