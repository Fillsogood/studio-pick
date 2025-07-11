package org.example.studiopick.web.review;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.review.dto.*;
import org.example.studiopick.application.review.service.ReviewReplyService;
import org.example.studiopick.application.review.service.ReviewService;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studios/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;
  private final ReviewReplyService reviewReplyService;

  @PostMapping
  public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
      @RequestParam Long userId,
      @ModelAttribute ReviewCreateRequest request
  ) {
    ReviewResponse response = reviewService.createReview(userId, request);
    return ResponseEntity.ok(new ApiResponse<>(true, response, "리뷰가 등록되었습니다."));
  }

  @PatchMapping("/{reviewId}")
  public ResponseEntity<ApiSuccessResponse<Void>> updateReview(
      @PathVariable Long reviewId,
      @RequestBody ReviewUpdateRequest request,
      @RequestParam Long userId // 인증 처리 전이므로 userId 직접 전달
  ) {
    reviewService.updateReview(reviewId, userId, request);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "리뷰가 수정되었습니다."));
  }

  @DeleteMapping("/{reviewId}")
  public ResponseEntity<ApiSuccessResponse<Void>> deleteReview(
      @PathVariable Long reviewId,
      @RequestParam Long userId
  ) {
    reviewService.deleteReview(reviewId, userId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "리뷰가 삭제되었습니다."));
  }

  // 운영자 리뷰 목록 + 답글 조회
  @GetMapping("/studio/{studioId}")
  public ApiResponse<List<ReviewWithReplyDto>> getStudioReviewsWithReplies(
      @PathVariable Long studioId
  ) {
    return new ApiResponse<>(true, reviewReplyService.getReviewsWithReplies(studioId), "리뷰 목록을 불러왔습니다.");
  }

  // 답글 등록 및 수정
  @PostMapping("/reply")
  public ApiResponse<ReviewReplyResponse> createOrUpdateReply(
      @RequestBody ReviewReplyRequest request
  ) {
    return new ApiResponse<>(true, reviewReplyService.createOrUpdateReply(request), "리뷰에 답글이 등록되었습니다.");
  }

  @DeleteMapping("/reply/{reviewId}")
  public ResponseEntity<ApiSuccessResponse<Void>> deleteReply(
      @PathVariable Long reviewId
  ) {
    reviewReplyService.deleteReply(reviewId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "답글이 삭제되었습니다."));
  }
}
