package org.example.studiopick.domain.review;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.ReviewStatus;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.User;

@Entity
@Table(name = "\"Review\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
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
    
    @Builder
    public Review(User user, Studio studio, Short rating, String comment, ReviewStatus status) {
        this.user = user;
        this.studio = studio;
        this.rating = rating;
        this.comment = comment;
        this.status = status != null ? status : ReviewStatus.VISIBLE;
    }
    
    public void updateReview(Short rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
    
    public void updateRating(Short rating) {
        if (rating != null && rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
    }
    
    public void updateComment(String comment) {
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
    
    public boolean isValidRating() {
        return rating != null && rating >= 1 && rating <= 5;
    }
}
