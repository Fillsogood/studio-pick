package org.example.studiopick.domain.review;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;

@Entity
@Table(name = "review_reply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReply extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Column(name = "content", nullable = false, length = 500)
  private String content;

  @Builder
  public ReviewReply(Review review, String content) {
    this.review = review;
    this.content = content;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}

