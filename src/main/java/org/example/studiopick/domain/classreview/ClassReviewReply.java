package org.example.studiopick.domain.classreview;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;

@Entity
@Table(name = "class_review_reply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassReviewReply extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private ClassReview classReview;

  @Column(name = "content", nullable = false, length = 500)
  private String content;

  @Builder
  public ClassReviewReply(ClassReview classReview, String content) {
    this.classReview = classReview;
    this.content = content;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}

