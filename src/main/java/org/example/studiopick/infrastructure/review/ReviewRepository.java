package org.example.studiopick.infrastructure.review;

import org.example.studiopick.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findByClassEntityId(Long classId);
  List<Review> findByUserId(Long ownerId);
}

