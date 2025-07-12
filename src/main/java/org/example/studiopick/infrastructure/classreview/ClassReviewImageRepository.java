package org.example.studiopick.infrastructure.classreview;

import org.example.studiopick.domain.classreview.ClassReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassReviewImageRepository extends JpaRepository<ClassReviewImage, Long> {
  List<ClassReviewImage> findByClassReviewId(Long classReviewId);
}

