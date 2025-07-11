package org.example.studiopick.application.review.service;

import org.example.studiopick.application.review.dto.ReviewReplyRequest;
import org.example.studiopick.application.review.dto.ReviewReplyResponse;
import org.example.studiopick.application.review.dto.ReviewWithReplyDto;

import java.util.List;

public interface ReviewReplyService {
  List<ReviewWithReplyDto> getReviewsWithReplies(Long studioId);
  ReviewReplyResponse createOrUpdateReply(ReviewReplyRequest request);
  void deleteReply(Long reviewId);
}
