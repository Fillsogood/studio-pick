package org.example.studiopick.infrastructure.review;

import org.example.studiopick.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByStudioId(Long studioId);
    Optional<Review> findByIdAndUserId(Long id, Long userId);
}