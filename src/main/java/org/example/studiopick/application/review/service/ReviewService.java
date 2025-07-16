package org.example.studiopick.application.review.service;

import org.example.studiopick.application.review.dto.*;

public interface ReviewService {
  ReviewResponse createReview(Long userId, ReviewCreateRequest classRequest);
  void updateReview(Long classReviewId, Long userId, ReviewUpdateRequest classRequest);
  void deleteReview(Long classReviewId, Long userId);
}
