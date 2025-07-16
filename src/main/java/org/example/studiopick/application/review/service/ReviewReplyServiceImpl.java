package org.example.studiopick.application.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.review.dto.ReviewReplyDto;
import org.example.studiopick.application.review.dto.ReviewReplyRequest;
import org.example.studiopick.application.review.dto.ReviewReplyResponse;
import org.example.studiopick.domain.review.Review;
import org.example.studiopick.domain.review.ReviewReply;
import org.example.studiopick.infrastructure.review.ReviewReplyRepository;
import org.example.studiopick.infrastructure.review.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {

  private final ReviewRepository classReviewRepository;
  private final ReviewReplyRepository reviewReplyRepository;

  @Override
  public List<ReviewReplyDto> getReviewsWithReplies(Long classId) {
    return classReviewRepository.findByClassEntityId(classId).stream()
        .map(review -> {
          String replyContent = reviewReplyRepository.findByClassReviewId(review.getId())
              .map(ReviewReply::getContent)
              .orElse(null);
          return new ReviewReplyDto(
              review.getId(),
              review.getComment(),
              review.getRating(),
              review.getUser().getNickname(),
              replyContent,
              review.getCreatedAt()
          );
        }).toList();
  }

  @Override
  public ReviewReplyResponse createOrUpdateReply(ReviewReplyRequest classRequest) {
    Review review = classReviewRepository.findById(classRequest.reviewId())
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    ReviewReply reply = reviewReplyRepository.findByClassReviewId(classRequest.reviewId())
        .map(r -> {
          r.updateContent(classRequest.content());
          return r;
        })
        .orElseGet(() -> reviewReplyRepository.save(
            ReviewReply.builder()
                .review(review)
                .content(classRequest.content())
                .build()
        ));

    return new ReviewReplyResponse(reply.getId(), reply.getContent(), "답글이 등록되었습니다.");
  }

  @Override
  @Transactional
  public void deleteReply(Long classReviewId) {
    ReviewReply reply = reviewReplyRepository.findByClassReviewId(classReviewId)
        .orElseThrow(() -> new RuntimeException("답글이 존재하지 않습니다."));

    reviewReplyRepository.delete(reply);
  }
}
