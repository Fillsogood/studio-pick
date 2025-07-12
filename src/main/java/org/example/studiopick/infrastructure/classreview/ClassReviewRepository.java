package org.example.studiopick.infrastructure.classreview;

import org.example.studiopick.domain.classreview.ClassReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassReviewRepository extends JpaRepository<ClassReview, Long> {
  List<ClassReview> findByClassEntityId(Long classId);
  List<ClassReview> findByUserId(Long ownerId);
}

