package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.artwork.AdminArtWorkDTOs.*;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.artwork.ArtworkComment;
import org.example.studiopick.domain.artwork.ArtworkLike;
import org.example.studiopick.domain.common.enums.ArtworkStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.infrastructure.artwork.ArtworkCommentRepository;
import org.example.studiopick.infrastructure.artwork.ArtworkLikeRepository;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.example.studiopick.infrastructure.report.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminArtWorkServiceImpl implements AdminArtWorkService {

  private final ArtworkRepository artworkRepository;
  private final ArtworkCommentRepository commentRepository;
  private final ArtworkLikeRepository likeRepository;
  private final ReportRepository reportRepository;
  private final PaginationValidator paginationValidator;

  @Override
  public AdminArtWorkListResponse getArtWorks(int page, int size, String status, String keyword) {
    Pageable pageable = PageRequest.of(paginationValidator.validatePage(page), paginationValidator.validateSize(size));
    Page<Artwork> artworks = artworkRepository.findAll(pageable);

    List<AdminArtWorkListResponse.AdminArtWorkSummaryDto> result = artworks.stream().map(a ->
        new AdminArtWorkListResponse.AdminArtWorkSummaryDto(
            a.getId(),
            a.getTitle(),
            a.getUser().getName(),
            a.getHideStatus().name(),
            a.getCreatedAt()
        )
    ).toList();

    return new AdminArtWorkListResponse(result, artworks.getTotalElements());
  }

  @Override
  public AdminArtWorkDetailResponse getArtWorkDetail(Long artworkId) {
    Artwork artwork = artworkRepository.findById(artworkId)
        .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다."));

    return new AdminArtWorkDetailResponse(
        artwork.getId(),
        artwork.getTitle(),
        artwork.getDescription(),
        artwork.getImageUrl(),
        artwork.getHashtags(),
        artwork.getHideStatus().name(),
        artwork.getShootingDate(),
        artwork.getShootingLocation(),
        artwork.getLikeCount(),
        artwork.getCommentCount(),
        artwork.getCreatedAt(),
        artwork.getUpdatedAt()
    );
  }

  @Override
  @Transactional
  public AdminArtWorkStatusResponse changeArtWorkStatus(Long artworkId, AdminArtWorkStatusCommand command) {
    Artwork artwork = artworkRepository.findById(artworkId)
        .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다."));

    artwork.ArtworkChangeStatus(command.status());

    String oldStatus = artwork.getHideStatus().name();
    artwork.ArtworkChangeStatus(command.status());

    return new AdminArtWorkStatusResponse(
        artwork.getId(),
        oldStatus,
        artwork.getHideStatus().name(),
        command.reason(),
        LocalDateTime.now()
    );
  }

  @Override
  @Transactional
  public void deleteArtWork(Long artworkId, String reason) {
    Artwork artwork = artworkRepository.findById(artworkId)
        .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다."));

    artworkRepository.delete(artwork);
  }

  @Override
  public AdminArtWorkStatsResponse getArtWorkStats() {
    long total = artworkRepository.count();
    long publicCount = artworkRepository.countByHideStatus(ArtworkStatus.PUBLIC);
    long privateCount = artworkRepository.countByHideStatus(ArtworkStatus.PRIVATE);
    long reportedCount = artworkRepository.countByHideStatus(ArtworkStatus.REPORTED);
    BigDecimal averageLikes = artworkRepository.calculateAverageLikeCount().orElse(BigDecimal.ZERO);

    return new AdminArtWorkStatsResponse(total, publicCount, privateCount, reportedCount, averageLikes);
  }

  @Override
  public AdminPopularArtWorkResponse getPopularArtWorks(String period, int limit) {
    List<Artwork> artworks = artworkRepository.findAllByOrderByLikeCountDesc(PageRequest.of(0, limit));

    List<AdminPopularArtWorkResponse.PopularArtWork> result = artworks.stream().map(a ->
        new AdminPopularArtWorkResponse.PopularArtWork(
            a.getId(),
            a.getTitle(),
            a.getUser().getName(),
            a.getLikeCount(),
            a.getCommentCount()
        )
    ).toList();

    return new AdminPopularArtWorkResponse(
        period,  // 예: "WEEKLY" 같은 값
        result
    );
  }

  @Override
  public List<ReportedArtWorkDto> getReportedArtWorks(int page, int size) {
    Pageable pageable = PageRequest.of(paginationValidator.validatePage(page), paginationValidator.validateSize(size));
    Page<Artwork> artworks = artworkRepository.findByHideStatus(ArtworkStatus.REPORTED, pageable);

    return artworks.stream().map(a -> {
      long reportCount = reportRepository.countByReportedTypeAndReportedId(ReportType.ARTWORK, a.getId());
      List<String> reasons = reportRepository.findTop5ByReportedTypeAndReportedIdOrderByCreatedAtDesc(ReportType.ARTWORK, a.getId())
          .stream()
          .map(Report::getReason)
          .toList();

      return new ReportedArtWorkDto(
          a.getId(),
          a.getTitle(),
          a.getUser().getName(),
          (int) reportCount,
          reasons,
          a.getHideStatus().name(),
          a.getCreatedAt()
      );
    }).toList();
  }

  @Override
  @Transactional
  public Long createArtWork(AdminArtWorkCreateCommand command, Long adminUserId) {
    Artwork artwork = Artwork.builder()
        .user(null)
        .studio(null)
        .title(command.title())
        .description(command.description())
        .imageUrl(command.imageUrl())
        .hashtags(command.hashtags())
        .isPublic(command.isPublic())
        .status(command.status())
        .shootingDate(command.shootingDate())
        .shootingLocation(command.shootingLocation())
        .build();

    artworkRepository.save(artwork);
    return artwork.getId();
  }

  @Override
  @Transactional
  public void likeArtWork(Long artworkId, Long userId) {
    Artwork artwork = artworkRepository.findById(artworkId)
        .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다."));

    boolean alreadyLiked = likeRepository.existsByArtworkIdAndUserId(artworkId, userId);
    if (!alreadyLiked) {
      ArtworkLike like = ArtworkLike.builder()
          .artwork(artwork)
          .user(null)
          .build();
      likeRepository.save(like);
    }
  }

  @Override
  @Transactional
  public void unlikeArtWork(Long artworkId, Long userId) {
    likeRepository.deleteByArtworkIdAndUserId(artworkId, userId);
  }

  @Override
  public List<AdminArtWorkCommentDto> getComments(Long artworkId, int page, int size) {
    Pageable pageable = PageRequest.of(paginationValidator.validatePage(page), paginationValidator.validateSize(size));
    Page<ArtworkComment> comments = commentRepository.findByArtworkId(artworkId, pageable);

    return comments.stream().map(c ->
        new AdminArtWorkCommentDto(
            c.getId(),
            c.getArtwork().getId(),
            c.getUser().getName(),
            c.getComment(),
            c.getCreatedAt()
        )
    ).toList();
  }

  @Override
  @Transactional
  public void deleteComment(Long commentId, String reason) {
    ArtworkComment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

    commentRepository.delete(comment);
  }

  @Override
  @Transactional
  public Long createComment(AdminArtWorkCommentCreateCommand command, Long adminUserId) {
    Artwork artwork = artworkRepository.findById(command.artworkId())
        .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다."));

    ArtworkComment comment = ArtworkComment.builder()
        .artwork(artwork)
        .user(null)
        .comment(command.comment())
        .build();

    commentRepository.save(comment);
    return comment.getId();
  }
}
