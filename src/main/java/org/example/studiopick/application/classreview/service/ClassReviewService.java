package org.example.studiopick.application.classreview.service;

import org.example.studiopick.application.classreview.dto.*;

public interface ClassReviewService {
  ClassReviewResponse createReview(Long userId, ClassReviewCreateRequest classRequest);
  void updateReview(Long classReviewId, Long userId, ClassReviewUpdateRequest classRequest);
  void deleteReview(Long classReviewId, Long userId);
}
