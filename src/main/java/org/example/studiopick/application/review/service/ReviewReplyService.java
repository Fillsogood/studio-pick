package org.example.studiopick.application.review.service;

import org.example.studiopick.application.review.dto.ReviewReplyDto;
import org.example.studiopick.application.review.dto.ReviewReplyRequest;
import org.example.studiopick.application.review.dto.ReviewReplyResponse;

import java.util.List;

public interface ReviewReplyService {
  List<ReviewReplyDto> getReviewsWithReplies(Long classId);
  ReviewReplyResponse createOrUpdateReply(ReviewReplyRequest classRequest);
  void deleteReply(Long reviewId);
}