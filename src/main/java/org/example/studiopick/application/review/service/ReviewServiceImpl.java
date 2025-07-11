package org.example.studiopick.application.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.review.dto.ReviewCreateRequest;
import org.example.studiopick.application.review.dto.ReviewResponse;
import org.example.studiopick.application.review.dto.ReviewUpdateRequest;
import org.example.studiopick.application.studio.FileUploader;
import org.example.studiopick.domain.common.enums.ReviewStatus;
import org.example.studiopick.domain.review.Review;
import org.example.studiopick.domain.review.ReviewImage;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.example.studiopick.infrastructure.review.ReviewImageRepository;
import org.example.studiopick.infrastructure.review.ReviewRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final JpaStudioRepository studioRepository;
  private final UserRepository userRepository;
  private final ReviewImageRepository reviewImageRepository;
  private final FileUploader fileUploader;

  @Override
  @Transactional
  public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    Studio studio = studioRepository.findById(request.studioId())
        .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));

    Review review = Review.builder()
        .user(user)
        .studio(studio)
        .rating(request.rating())
        .comment(request.comment())
        .status(ReviewStatus.VISIBLE)
        .build();
    reviewRepository.save(review);

    if (request.images() != null) {
      for (MultipartFile image : request.images()) {
        String url = fileUploader.upload(image, "reviews/studio");
        ReviewImage reviewImage = ReviewImage.builder()
            .review(review)
            .imageUrl(url)
            .build();
        reviewImageRepository.save(reviewImage);
      }
    }

    return new ReviewResponse(review.getId(), "리뷰 작성이 완료되었습니다.");
  }

  @Override
  @Transactional
  public void updateReview(Long reviewId, Long userId, ReviewUpdateRequest request) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

    if (!review.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
    }

    review.updateReview(request.rating(), request.comment());
  }

  @Override
  @Transactional
  public void deleteReview(Long reviewId, Long userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

    // 권한 확인
    if (!review.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
    }

    // 이미지 S3 삭제
    for (ReviewImage image : review.getImages()) {
      fileUploader.delete(image.getImageUrl());
    }

    // 리뷰 삭제
    reviewRepository.delete(review);
  }
}
