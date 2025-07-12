package org.example.studiopick.application.classreview.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classreview.dto.*;
import org.example.studiopick.application.studio.FileUploader;
import org.example.studiopick.domain.class_entity.ClassEntity;
import org.example.studiopick.domain.classreview.ClassReview;
import org.example.studiopick.domain.classreview.ClassReviewImage;
import org.example.studiopick.domain.common.enums.ReviewStatus;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.example.studiopick.infrastructure.classes.ClassRepository;
import org.example.studiopick.infrastructure.classreview.ClassReviewImageRepository;
import org.example.studiopick.infrastructure.classreview.ClassReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassReviewServiceImpl implements ClassReviewService {

  private final ClassReviewRepository reviewRepository;
  private final ClassReviewImageRepository imageRepository;
  private final UserRepository userRepository;
  private final ClassRepository classRepository;
  private final FileUploader fileUploader;


  @Override
  @Transactional
  public ClassReviewResponse createReview(Long userId, ClassReviewCreateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자 없음"));
    ClassEntity classEntity = classRepository.findById(request.classId())
        .orElseThrow(() -> new RuntimeException("클래스 없음"));

    ClassReview review = ClassReview.builder()
        .user(user)
        .classEntity(classEntity)
        .rating(request.rating())
        .comment(request.comment())
        .status(ReviewStatus.VISIBLE)
        .build();
    reviewRepository.save(review);

    if (request.imageUrls() != null) {
      for (MultipartFile image : request.imageUrls()) {
        String url = fileUploader.upload(image, "reviews/class");
        ClassReviewImage reviewImage = ClassReviewImage.builder()
            .classReview(review)
            .imageUrl(url)
            .build();
        imageRepository.save(reviewImage);
      }
    }

    return new ClassReviewResponse(review.getId(), "리뷰 작성이 완료되었습니다.");
  }

  @Override
  @Transactional
  public void updateReview(Long reviewId, Long userId, ClassReviewUpdateRequest request) {
    ClassReview review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    if (!review.getUser().getId().equals(userId)) {
      throw new RuntimeException("리뷰 작성자만 수정할 수 있습니다.");
    }

    review.update(request.rating(), request.comment());
  }


  @Override
  @Transactional
  public void deleteReview(Long reviewId, Long userId) {
    ClassReview classReview = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    if (!classReview.getUser().getId().equals(userId)) {
      throw new RuntimeException("리뷰 작성자만 삭제할 수 있습니다.");
    }

    // 이미지 먼저 삭제
    for (ClassReviewImage image : classReview.getImages()) {
      fileUploader.delete(image.getImageUrl());
    }
    // 리뷰 삭제
    reviewRepository.delete(classReview);
  }

}
