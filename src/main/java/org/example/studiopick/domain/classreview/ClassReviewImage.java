package org.example.studiopick.domain.classreview;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.review.Review;

@Entity
@Table(name = "class_review_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassReviewImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id")
  private ClassReview classReview;

  @Column(name = "image_url", nullable = false, length = 255)
  private String imageUrl;

  public void setClassReview(ClassReview review) {
    this.classReview = review;
  }

  @Builder
  public ClassReviewImage(ClassReview classReview, String imageUrl) {
    this.classReview = classReview;
    this.imageUrl = imageUrl;
  }

  public void setReview(ClassReview classReview) {this.classReview = classReview;}
}

