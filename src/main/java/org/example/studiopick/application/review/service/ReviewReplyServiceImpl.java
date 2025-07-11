package org.example.studiopick.application.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.review.dto.ReviewReplyRequest;
import org.example.studiopick.application.review.dto.ReviewReplyResponse;
import org.example.studiopick.application.review.dto.ReviewWithReplyDto;
import org.example.studiopick.domain.review.Review;
import org.example.studiopick.domain.review.ReviewReply;
import org.example.studiopick.infrastructure.review.ReviewReplyRepository;
import org.example.studiopick.infrastructure.review.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {

  private final ReviewRepository reviewRepository;
  private final ReviewReplyRepository replyRepository;

  @Override
  public List<ReviewWithReplyDto> getReviewsWithReplies(Long studioId) {
    return reviewRepository.findByStudioId(studioId).stream()
        .map(review -> {
          String replyContent = replyRepository.findByReviewId(review.getId())
              .map(ReviewReply::getContent)
              .orElse(null);
          return new ReviewWithReplyDto(
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
  public ReviewReplyResponse createOrUpdateReply(ReviewReplyRequest request) {
    Review review = reviewRepository.findById(request.reviewId())
        .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

    ReviewReply reply = replyRepository.findByReviewId(request.reviewId())
        .map(r -> {
          r.updateContent(request.content());
          return r;
        })
        .orElseGet(() -> replyRepository.save(
            ReviewReply.builder()
                .review(review)
                .content(request.content())
                .build()
        ));

    return new ReviewReplyResponse(reply.getId(), reply.getContent(), "답글이 등록되었습니다.");
  }

  @Override
  @Transactional
  public void deleteReply(Long reviewId) {
    ReviewReply reply = replyRepository.findByReviewId(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("해당 리뷰의 답글이 존재하지 않습니다."));

    replyRepository.delete(reply);
  }
}
