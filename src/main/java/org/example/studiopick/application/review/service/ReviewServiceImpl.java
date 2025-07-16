package org.example.studiopick.application.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.review.dto.*;
import org.example.studiopick.application.studio.FileUploader;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.review.Review;
import org.example.studiopick.domain.review.ReviewImage;
import org.example.studiopick.domain.common.enums.ReviewStatus;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.example.studiopick.infrastructure.review.ReviewImageRepository;
import org.example.studiopick.infrastructure.review.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewImageRepository imageRepository;
  private final JpaUserRepository userRepository;
  private final JpaWorkShopRepository jpaWorkShopRepository;
  private final FileUploader fileUploader;


  @Override
  @Transactional
  public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자 없음"));
    WorkShop workShop = jpaWorkShopRepository.findById(request.classId())
        .orElseThrow(() -> new RuntimeException("클래스 없음"));

    Review review = Review.builder()
        .user(user)
        .workShop(workShop)
        .rating(request.rating())
        .comment(request.comment())
        .status(ReviewStatus.VISIBLE)
        .build();
    reviewRepository.save(review);

    if (request.imageUrls() != null) {
      for (MultipartFile image : request.imageUrls()) {
        String url = fileUploader.upload(image, "reviews/class");
        ReviewImage reviewImage = ReviewImage.builder()
            .review(review)
            .imageUrl(url)
            .build();
        imageRepository.save(reviewImage);
      }
    }

    return new ReviewResponse(review.getId(), "리뷰 작성이 완료되었습니다.");
  }

  @Override
  @Transactional
  public void updateReview(Long reviewId, Long userId, ReviewUpdateRequest request) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    if (!review.getUser().getId().equals(userId)) {
      throw new RuntimeException("리뷰 작성자만 수정할 수 있습니다.");
    }

    review.update(request.rating(), request.comment());
  }


  @Override
  @Transactional
  public void deleteReview(Long reviewId, Long userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    if (!review.getUser().getId().equals(userId)) {
      throw new RuntimeException("리뷰 작성자만 삭제할 수 있습니다.");
    }

    // 이미지 먼저 삭제
    for (ReviewImage image : review.getImages()) {
      fileUploader.delete(image.getImageUrl());
    }
    // 리뷰 삭제
    reviewRepository.delete(review);
  }

  @Override
  public ReviewDetailResponse getReviewDetail(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    List<String> imageUrls = review.getImages().stream()
        .map(ReviewImage::getImageUrl)
        .collect(Collectors.toList());

    return new ReviewDetailResponse(
        review.getId(),
        review.getUser().getId(),
        review.getUser().getNickname(),
        review.getStudio().getId(),
        review.getWorkShop().getId(),
        review.getRating(),
        review.getComment(),
        review.getStatus(),
        imageUrls,
        review.getCreatedAt(),
        review.getUpdatedAt()
    );
  }

  @Override
  public List<ReviewSummaryDto> getReviewsByStudio(Long studioId, int page, int size) {
    return reviewRepository.findByStudioIdOrWorkshopId(studioId).stream()
        .filter(Review::isPubliclyVisible)
        .skip((long) (page - 1) * size)
        .limit(size)
        .map(r -> new ReviewSummaryDto(
            r.getId(),
            r.getUser().getId(),
            r.getUser().getNickname(),
            r.getRating(),
            r.getComment(),
            r.getStatus(),
            r.getCreatedAt()
        ))
        .collect(Collectors.toList());
  }

  @Override
  public List<ReviewSummaryDto> getReviewsByWorkshop(Long workshopId, int page, int size) {
    return reviewRepository.findByStudioIdOrWorkshopId(workshopId).stream()
        .filter(Review::isPubliclyVisible)
        .skip((long) (page - 1) * size)
        .limit(size)
        .map(r -> new ReviewSummaryDto(
            r.getId(),
            r.getUser().getId(),
            r.getUser().getNickname(),
            r.getRating(),
            r.getComment(),
            r.getStatus(),
            r.getCreatedAt()
        ))
        .collect(Collectors.toList());
  }

}
