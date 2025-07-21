package org.example.studiopick.infrastructure.review;

import org.example.studiopick.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  @Query("SELECT r FROM Review r WHERE (r.studio.id = :contentId OR r.workShop.id = :contentId) AND r.status <> 'DELETED'")
  List<Review> findByStudioIdOrWorkshopId(Long contentId);

  // 평점 평균 구하는 쿼리 추가
  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.workShop.id = :workshopId AND r.status = 'VISIBLE'")
  Double getAverageRatingByWorkshopId(@Param("workshopId") Long workshopId);

}

