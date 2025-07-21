package org.example.studiopick.application.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.review.dto.*;
import org.example.studiopick.application.studio.FileUploader;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.review.Review;
import org.example.studiopick.domain.review.ReviewImage;
import org.example.studiopick.domain.common.enums.ReviewStatus;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.example.studiopick.infrastructure.review.ReviewImageRepository;
import org.example.studiopick.infrastructure.review.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewImageRepository imageRepository;
  private final JpaUserRepository userRepository;
  private final JpaWorkShopRepository jpaWorkShopRepository;
  private final JpaStudioRepository studioRepository;
  private final FileUploader fileUploader;

  // ✅ 리뷰 생성
  @Override
  @Transactional
  public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
    // 디버깅용 로그
    System.out.println("=== 리뷰 생성 요청 ===");
    System.out.println("userId: " + userId);
    System.out.println("type: " + request.type());
    System.out.println("targetId: " + request.targetId());
    System.out.println("rating: " + request.rating());
    System.out.println("comment: " + request.comment());
    System.out.println("imageUrls: " + request.imageUrls());
    System.out.println("=====================");

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자 없음"));

    Review review;

    switch (request.type().toLowerCase()) {
      case "studio" -> {
        Studio studio = studioRepository.findById(request.targetId())
                .orElseThrow(() -> new RuntimeException("스튜디오 없음"));
        review = Review.builder()
                .user(user)
                .studio(studio)
                .rating(request.rating())
                .comment(request.comment())
                .status(ReviewStatus.VISIBLE)
                .build();
      }
      case "workshop" -> {
        WorkShop workShop = jpaWorkShopRepository.findById(request.targetId())
                .orElseThrow(() -> new RuntimeException("공방 없음"));
        review = Review.builder()
                .user(user)
                .workShop(workShop)
                .rating(request.rating())
                .comment(request.comment())
                .status(ReviewStatus.VISIBLE)
                .build();
      }
      default -> throw new RuntimeException("리뷰 대상 타입이 유효하지 않습니다.");
    }

    reviewRepository.save(review);

    // 이미지 URL 처리 (List<String> 직접 사용)
    System.out.println("=== 이미지 처리 시작 ===");
    System.out.println("원본 imageUrls: " + request.imageUrls());
    
    if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
      System.out.println("이미지 URL 개수: " + request.imageUrls().size());
      
      for (String imageUrl : request.imageUrls()) {
        System.out.println("처리 중인 URL: " + imageUrl);
        
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
          ReviewImage reviewImage = ReviewImage.builder()
              .review(review)
              .imageUrl(imageUrl.trim())
              .build();
          ReviewImage savedImage = imageRepository.save(reviewImage);
          System.out.println("저장된 이미지 ID: " + savedImage.getId());
        }
      }
    } else {
      System.out.println("imageUrls가 null이거나 비어있음");
    }
    System.out.println("=== 이미지 처리 완료 ===");

    return new ReviewResponse(review.getId(), "리뷰 작성이 완료되었습니다.");
  }

  // ✅ 리뷰 수정
  @Override
  @Transactional
  public void updateReview(Long reviewId, Long userId, ReviewUpdateRequest request) {
    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    if (!review.getUser().getId().equals(userId)) {
      throw new RuntimeException("리뷰 작성자만 수정할 수 있습니다.");
    }

    // 리뷰 기본 정보 업데이트
    review.update(request.rating(), request.comment());

    // 기존 이미지들 삭제 (항상 실행)
    System.out.println("=== 리뷰 수정 시작 ===");
    System.out.println("기존 이미지 개수: " + review.getImages().size());
    
    // 강제로 모든 기존 이미지 삭제
    List<ReviewImage> existingImages = new ArrayList<>(review.getImages());
    for (ReviewImage image : existingImages) {
      System.out.println("삭제 중인 이미지: " + image.getImageUrl());
      try {
        fileUploader.delete(image.getImageUrl());
      } catch (Exception e) {
        System.out.println("S3 삭제 실패: " + e.getMessage());
      }
      imageRepository.delete(image);
    }
    
    // review 엔티티에서 이미지 리스트 초기화
    review.getImages().clear();
    System.out.println("기존 이미지 삭제 완료");

    // 새 이미지 URL 처리
    System.out.println("새 이미지 URL: " + request.imageUrl());
    if (request.imageUrl() != null && !request.imageUrl().trim().isEmpty()) {
      // 새 이미지 URL들을 콤마로 분리하여 저장
      String[] imageUrlArray = request.imageUrl().split(",");
      System.out.println("새 이미지 개수: " + imageUrlArray.length);
      
      for (String imageUrl : imageUrlArray) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
          System.out.println("저장 중인 새 이미지: " + imageUrl.trim());
          ReviewImage reviewImage = ReviewImage.builder()
              .review(review)
              .imageUrl(imageUrl.trim())
              .build();
          imageRepository.save(reviewImage);
        }
      }
    } else {
      System.out.println("새 이미지가 없습니다.");
    }
    System.out.println("=== 리뷰 수정 완료 ===");
  }

  // ✅ 리뷰 삭제
  @Override
  @Transactional
  public void deleteReview(Long reviewId, Long userId) {
    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    if (!review.getUser().getId().equals(userId)) {
      throw new RuntimeException("리뷰 작성자만 삭제할 수 있습니다.");
    }

    for (ReviewImage image : review.getImages()) {
      fileUploader.delete(image.getImageUrl());
    }
    reviewRepository.delete(review);
  }
  // 평균 평점 조회 (워크샵 기준)
  @Override
  public Double getAverageRatingByWorkshopId(Long workshopId) {
    Double avg = reviewRepository.getAverageRatingByWorkshopId(workshopId);
    return avg != null ? avg : 0.0;
  }

  // ✅ 리뷰 상세 조회
  @Override
  public ReviewDetailResponse getReviewDetail(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    List<String> imageUrls = review.getImages().stream()
            .map(ReviewImage::getImageUrl)
            .collect(Collectors.toList());

    // 스튜디오와 공방 ID를 안전하게 가져오기
    Long studioId = review.getStudio() != null ? review.getStudio().getId() : null;
    Long workshopId = review.getWorkShop() != null ? review.getWorkShop().getId() : null;

    return new ReviewDetailResponse(
            review.getId(),
            review.getUser().getId(),
            review.getUser().getNickname(),
            studioId,
            workshopId,
            review.getRating(),
            review.getComment(),
            review.getStatus(),
            imageUrls,
            review.getCreatedAt(),
            review.getUpdatedAt()
    );
  }

  // ✅ 스튜디오 리뷰 목록
  @Override
  public List<ReviewSummaryDto> getReviewsByStudio(Long studioId, int page, int size) {
    System.out.println("=== getReviewsByStudio 시작 ===");
    System.out.println("studioId: " + studioId);
    System.out.println("page: " + page);
    System.out.println("size: " + size);
    
    List<Review> allReviews = reviewRepository.findByStudioIdOrWorkshopId(studioId);
    System.out.println("전체 리뷰 개수: " + allReviews.size());
    
    List<Review> visibleReviews = allReviews.stream()
            .filter(Review::isPubliclyVisible)
            .collect(Collectors.toList());
    System.out.println("공개 리뷰 개수: " + visibleReviews.size());
    
    List<ReviewSummaryDto> result = visibleReviews.stream()
            .skip((long) (page - 1) * size)
            .limit(size)
            .map(r -> {
                List<String> imageUrls = r.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList());
                
                return new ReviewSummaryDto(
                        r.getId(),
                        r.getUser().getId(),
                        r.getUser().getNickname(),
                        r.getRating(),
                        r.getComment(),
                        r.getStatus(),
                        r.getCreatedAt(),
                        imageUrls
                );
            })
            .collect(Collectors.toList());
    
    System.out.println("반환할 리뷰 개수: " + result.size());
    System.out.println("=== getReviewsByStudio 완료 ===");
    
    return result;
  }

  // ✅ 공방 리뷰 목록
  @Override
  public List<ReviewSummaryDto> getReviewsByWorkshop(Long workshopId, int page, int size) {
    return reviewRepository.findByStudioIdOrWorkshopId(workshopId).stream()
            .filter(Review::isPubliclyVisible)
            .skip((long) (page - 1) * size)
            .limit(size)
            .map(r -> {
                List<String> imageUrls = r.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList());
                
                return new ReviewSummaryDto(
                        r.getId(),
                        r.getUser().getId(),
                        r.getUser().getNickname(),
                        r.getRating(),
                        r.getComment(),
                        r.getStatus(),
                        r.getCreatedAt(),
                        imageUrls
                );
            })
            .collect(Collectors.toList());
  }

  // ✅ 리뷰 이미지 업로드
  @Override
  public List<String> uploadReviewImages(List<MultipartFile> files) {
    return files.stream()
            .map(file -> fileUploader.upload(file, "reviews"))
            .collect(Collectors.toList());
  }

  // ✅ 리뷰 이미지 삭제
  @Override
  public void deleteReviewImages(List<String> fileUrls) {
    for (String url : fileUrls) {
      fileUploader.delete(url);
    }
  }
}
