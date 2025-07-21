package org.example.studiopick.application.review.service;

import org.example.studiopick.application.review.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
  ReviewResponse createReview(Long userId, ReviewCreateRequest request);
  void updateReview(Long classReviewId, Long userId, ReviewUpdateRequest classRequest);
  void deleteReview(Long classReviewId, Long userId);

  ReviewDetailResponse getReviewDetail(Long reviewId);
  List<String> uploadReviewImages(List<MultipartFile> files);
  void deleteReviewImages(List<String> fileUrls);
  List<ReviewSummaryDto> getReviewsByStudio(Long studioId, int page, int size);
  List<ReviewSummaryDto> getReviewsByWorkshop(Long workshopId, int page, int size);

  Double getAverageRatingByWorkshopId(Long workshopId);

}
