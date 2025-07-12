package org.example.studiopick.application.classreview.service;

import org.example.studiopick.application.classreview.dto.ClassReviewReplyDto;
import org.example.studiopick.application.classreview.dto.ClassReviewReplyRequest;
import org.example.studiopick.application.classreview.dto.ClassReviewReplyResponse;

import java.util.List;

public interface ClassReviewReplyService {
  List<ClassReviewReplyDto> getReviewsWithReplies(Long classId);
  ClassReviewReplyResponse createOrUpdateReply(ClassReviewReplyRequest classRequest);
  void deleteReply(Long reviewId);
}