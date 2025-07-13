package org.example.studiopick.web.classreview;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classreview.dto.*;
import org.example.studiopick.application.classreview.service.ClassReviewReplyService;
import org.example.studiopick.application.classreview.service.ClassReviewService;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes/reviews")
@RequiredArgsConstructor
public class ClassReviewController {

  private final ClassReviewService classReviewService;
  private final ClassReviewReplyService replyService;

  @PostMapping
  public ResponseEntity<ApiResponse<ClassReviewResponse>> createReview(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @ModelAttribute ClassReviewCreateRequest request
  ) {
    // 토큰에서 직접 사용자 ID 추출
    Long userId = userPrincipal.getUserId();
    ClassReviewResponse response = classReviewService.createReview(userId, request);
    return ResponseEntity.ok(new ApiResponse<>(true, response, "리뷰가 등록되었습니다."));
  }

  @PatchMapping("/{reviewId}")
  public ResponseEntity<ApiSuccessResponse<Void>> updateReview(
      @PathVariable Long reviewId,
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @ModelAttribute ClassReviewUpdateRequest request
  ) {
    // 토큰에서 직접 사용자 ID 추출
    Long userId = userPrincipal.getUserId();
    classReviewService.updateReview(reviewId, userId, request);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "리뷰가 수정되었습니다."));
  }

  @DeleteMapping("/{reviewId}")
  public ResponseEntity<ApiSuccessResponse<Void>> deleteReview(
      @PathVariable Long reviewId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    // 토큰에서 직접 사용자 ID 추출
    Long userId = userPrincipal.getUserId();
    classReviewService.deleteReview(reviewId, userId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "리뷰가 삭제되었습니다."));
  }

  // 운영자 리뷰 목록 + 답글 조회
  @GetMapping("/classes/{classId}")
  public ApiResponse<List<ClassReviewReplyDto>> getReviewsWithReplies(
      @PathVariable Long classId
  ) {
    return new ApiResponse<>(true, replyService.getReviewsWithReplies(classId), "리뷰 목록을 불러왔습니다.");
  }

  // 답글 등록 및 수정
  @PostMapping("/reply")
  public ApiResponse<ClassReviewReplyResponse> createOrUpdateReply(
      @RequestBody ClassReviewReplyRequest request
  ) {
    return new ApiResponse<>(true, replyService.createOrUpdateReply(request), "리뷰에 답글이 등록되었습니다.");
  }

  @DeleteMapping("/reply/{reviewId}")
  public ResponseEntity<ApiSuccessResponse<Void>> deleteReply(
      @PathVariable Long reviewId
  ) {
    replyService.deleteReply(reviewId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "답글이 삭제되었습니다."));
  }
}
