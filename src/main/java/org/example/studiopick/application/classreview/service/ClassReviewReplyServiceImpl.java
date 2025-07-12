package org.example.studiopick.application.classreview.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classreview.dto.ClassReviewReplyDto;
import org.example.studiopick.application.classreview.dto.ClassReviewReplyRequest;
import org.example.studiopick.application.classreview.dto.ClassReviewReplyResponse;
import org.example.studiopick.domain.classreview.ClassReview;
import org.example.studiopick.domain.classreview.ClassReviewReply;
import org.example.studiopick.infrastructure.classreview.ClassReviewReplyRepository;
import org.example.studiopick.infrastructure.classreview.ClassReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassReviewReplyServiceImpl implements ClassReviewReplyService {

  private final ClassReviewRepository classReviewRepository;
  private final ClassReviewReplyRepository classReviewReplyRepository;

  @Override
  public List<ClassReviewReplyDto> getReviewsWithReplies(Long classId) {
    return classReviewRepository.findByClassEntityId(classId).stream()
        .map(review -> {
          String replyContent = classReviewReplyRepository.findByClassReviewId(review.getId())
              .map(ClassReviewReply::getContent)
              .orElse(null);
          return new ClassReviewReplyDto(
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
  public ClassReviewReplyResponse createOrUpdateReply(ClassReviewReplyRequest classRequest) {
    ClassReview classReview = classReviewRepository.findById(classRequest.reviewId())
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    ClassReviewReply reply = classReviewReplyRepository.findByClassReviewId(classRequest.reviewId())
        .map(r -> {
          r.updateContent(classRequest.content());
          return r;
        })
        .orElseGet(() -> classReviewReplyRepository.save(
            ClassReviewReply.builder()
                .classReview(classReview)
                .content(classRequest.content())
                .build()
        ));

    return new ClassReviewReplyResponse(reply.getId(), reply.getContent(), "답글이 등록되었습니다.");
  }

  @Override
  @Transactional
  public void deleteReply(Long classReviewId) {
    ClassReviewReply reply = classReviewReplyRepository.findByClassReviewId(classReviewId)
        .orElseThrow(() -> new RuntimeException("답글이 존재하지 않습니다."));

    classReviewReplyRepository.delete(reply);
  }
}
