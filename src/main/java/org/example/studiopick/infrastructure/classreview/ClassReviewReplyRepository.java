package org.example.studiopick.infrastructure.classreview;

import org.example.studiopick.domain.classreview.ClassReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassReviewReplyRepository extends JpaRepository<ClassReviewReply, Long> {
  Optional<ClassReviewReply> findByClassReviewId(Long classReviewId);
}
