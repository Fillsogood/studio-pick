package org.example.studiopick.infrastructure.review;

import org.example.studiopick.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  @Query("SELECT r FROM Review r WHERE (r.studio.id = :contentId OR r.workShop.id = :contentId) AND r.status <> 'DELETED'")
  List<Review> findByStudioIdOrWorkshopId(Long contentId);
}

