package org.example.studiopick.infrastructure.review;

import org.example.studiopick.domain.review.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
  List<ReviewImage> findByReviewId(Long reviewId);
}
