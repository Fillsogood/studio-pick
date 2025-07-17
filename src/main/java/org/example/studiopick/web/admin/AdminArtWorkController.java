package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.admin.AdminArtWorkService;
import org.example.studiopick.application.admin.dto.artwork.AdminArtWorkDTOs.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/artworks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin ArtWork", description = "관리자 작품 관리 API")
public class AdminArtWorkController {

  private final AdminArtWorkService adminArtWorkService;

  @GetMapping
  @Operation(summary = "작품 목록 조회")
  public ResponseEntity<ApiResponse<AdminArtWorkListResponse>> getArtWorks(
      @RequestParam int page,
      @RequestParam int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    AdminArtWorkListResponse response = adminArtWorkService.getArtWorks(page, size, status, keyword);
    ApiResponse<AdminArtWorkListResponse> apiResponse = new ApiResponse<>(true, response, "작품 목록을 조회했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/{artworkId}")
  @Operation(summary = "작품 상세 조회")
  public ResponseEntity<ApiResponse<AdminArtWorkDetailResponse>> getArtWorkDetail(@PathVariable Long artworkId) {
    AdminArtWorkDetailResponse response = adminArtWorkService.getArtWorkDetail(artworkId);
    ApiResponse<AdminArtWorkDetailResponse> apiResponse = new ApiResponse<>(true, response, "작품 상세 정보를 조회했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @PatchMapping("/{artworkId}/status")
  @Operation(summary = "작품 상태 변경")
  public ResponseEntity<ApiResponse<AdminArtWorkStatusResponse>> changeArtWorkStatus(
      @PathVariable Long artworkId,
      @Valid @RequestBody AdminArtWorkStatusCommand command) {
    AdminArtWorkStatusResponse response = adminArtWorkService.changeArtWorkStatus(artworkId, command);
    ApiResponse<AdminArtWorkStatusResponse> apiResponse = new ApiResponse<>(true, response, "작품 상태를 변경했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{artworkId}")
  @Operation(summary = "작품 삭제")
  public ResponseEntity<ApiResponse<Void>> deleteArtWork(@PathVariable Long artworkId, @RequestParam String reason) {
    adminArtWorkService.deleteArtWork(artworkId, reason);
    ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "작품을 삭제했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/stats")
  @Operation(summary = "작품 통계 조회")
  public ResponseEntity<ApiResponse<AdminArtWorkStatsResponse>> getArtWorkStats() {
    AdminArtWorkStatsResponse response = adminArtWorkService.getArtWorkStats();
    ApiResponse<AdminArtWorkStatsResponse> apiResponse = new ApiResponse<>(true, response, "작품 통계를 조회했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/popular")
  @Operation(summary = "인기 작품 조회")
  public ResponseEntity<ApiResponse<AdminPopularArtWorkResponse>> getPopularArtWorks(
      @RequestParam String period,
      @RequestParam int limit) {
    AdminPopularArtWorkResponse response = adminArtWorkService.getPopularArtWorks(period, limit);
    ApiResponse<AdminPopularArtWorkResponse> apiResponse = new ApiResponse<>(true, response, "인기 작품을 조회했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/reported")
  @Operation(summary = "신고된 작품 목록")
  public ResponseEntity<ApiResponse<List<ReportedArtWorkDto>>> getReportedArtWorks(
      @RequestParam int page,
      @RequestParam int size) {
    List<ReportedArtWorkDto> response = adminArtWorkService.getReportedArtWorks(page, size);
    ApiResponse<List<ReportedArtWorkDto>> apiResponse = new ApiResponse<>(true, response, "신고된 작품 목록을 조회했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping
  @Operation(summary = "작품 생성")
  public ResponseEntity<ApiResponse<Long>> createArtWork(@Valid @RequestBody AdminArtWorkCreateCommand command, @RequestParam Long adminUserId) {
    Long response = adminArtWorkService.createArtWork(command, adminUserId);
    ApiResponse<Long> apiResponse = new ApiResponse<>(true, response, "작품을 생성했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/{artworkId}/like")
  @Operation(summary = "작품 좋아요")
  public ResponseEntity<ApiResponse<Void>> likeArtWork(@PathVariable Long artworkId, @RequestParam Long userId) {
    adminArtWorkService.likeArtWork(artworkId, userId);
    ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "작품에 좋아요를 추가했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{artworkId}/like")
  @Operation(summary = "작품 좋아요 취소")
  public ResponseEntity<ApiResponse<Void>> unlikeArtWork(@PathVariable Long artworkId, @RequestParam Long userId) {
    adminArtWorkService.unlikeArtWork(artworkId, userId);
    ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "작품 좋아요를 취소했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/{artworkId}/comments")
  @Operation(summary = "작품 댓글 목록 조회")
  public ResponseEntity<ApiResponse<List<AdminArtWorkCommentDto>>> getComments(
      @PathVariable Long artworkId,
      @RequestParam int page,
      @RequestParam int size) {
    List<AdminArtWorkCommentDto> response = adminArtWorkService.getComments(artworkId, page, size);
    ApiResponse<List<AdminArtWorkCommentDto>> apiResponse = new ApiResponse<>(true, response, "작품 댓글 목록을 조회했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/comments/{commentId}")
  @Operation(summary = "작품 댓글 삭제")
  public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId, @RequestParam String reason) {
    adminArtWorkService.deleteComment(commentId, reason);
    ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "작품 댓글을 삭제했습니다.");
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/comments")
  @Operation(summary = "작품 댓글 생성")
  public ResponseEntity<ApiResponse<Long>> createComment(
      @Valid @RequestBody AdminArtWorkCommentCreateCommand command,
      @RequestParam Long adminUserId) {
    Long response = adminArtWorkService.createComment(command, adminUserId);
    ApiResponse<Long> apiResponse = new ApiResponse<>(true, response, "작품 댓글을 생성했습니다.");
    return ResponseEntity.ok(apiResponse);
  }
}
