package org.example.studiopick.domain.review;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.ReviewStatus;
import org.example.studiopick.domain.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workshop_id")
  private WorkShop workShop;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studio_id")
  private Studio studio;

  @Column(name = "rating")
  private Short rating;

  @Column(name = "comment", columnDefinition = "TEXT")
  private String comment;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ReviewStatus status = ReviewStatus.VISIBLE;

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReviewImage> images = new ArrayList<>();

  @Builder
  public Review(WorkShop workShop, Studio studio, User user, Short rating, String comment, ReviewStatus status) {
    this.user = user;
    this.workShop = workShop;
    this.studio = studio;
    this.rating = rating;
    this.comment = comment;
    this.status = status != null ? status : ReviewStatus.VISIBLE;
  }

  public void update(Short rating, String comment) {
    this.rating = rating;
    this.comment = comment;
  }

  public void changeStatus(ReviewStatus status) {
    this.status = status;
  }

  public void hide() {
    this.status = ReviewStatus.HIDDEN;
  }

  public void show() {
    this.status = ReviewStatus.VISIBLE;
  }

  public void delete() {
    this.status = ReviewStatus.DELETED;
  }

  public boolean isVisible() {
    return this.status == ReviewStatus.VISIBLE;
  }

  public boolean isHidden() {
    return this.status == ReviewStatus.HIDDEN;
  }

  public boolean isDeleted() {
    return this.status == ReviewStatus.DELETED;
  }

  public void report() {
    this.status = ReviewStatus.REPORTED;
  }

  public void restore() {
    this.status = ReviewStatus.VISIBLE;
  }

  public boolean isReported() {
    return this.status == ReviewStatus.REPORTED;
  }

  public boolean isPubliclyVisible() {
    return this.status == ReviewStatus.VISIBLE;
  }

  public boolean isValidRating() {
    return rating != null && rating >= 1 && rating <= 5;
  }
}