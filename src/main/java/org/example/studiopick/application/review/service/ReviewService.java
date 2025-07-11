package org.example.studiopick.application.review.service;

import org.example.studiopick.application.review.dto.ReviewCreateRequest;
import org.example.studiopick.application.review.dto.ReviewResponse;
import org.example.studiopick.application.review.dto.ReviewUpdateRequest;

public interface ReviewService {
  ReviewResponse createReview(Long userId, ReviewCreateRequest request);
  void updateReview(Long reviewId, Long userId, ReviewUpdateRequest request);
  void deleteReview(Long reviewId, Long userId);
}
