package org.example.studiopick.domain.artwork;

import jakarta.persistence.*;
import lombok.*;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.ArtworkStatus;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.entity.User;

// Artwork 엔티티 - 작품 피드용
@Entity
@Table(name = "\"Artwork\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Artwork extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id")
    private Studio studio;
    
    @Column(name = "title", nullable = false, length = 50)
    private String title;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;
    
    @Column(name = "hashtags", length = 255)
    private String hashtags;

    // [추가] 촬영일자
    @Column(name = "shooting_date")
    private String shootingDate;

    // [추가] 촬영장소
    @Column(name = "shooting_location", length = 255)
    private String shootingLocation;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ArtworkStatus status = ArtworkStatus.PUBLIC;

    @Column(name = "display_order", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer order;
    
    @Builder
    public Artwork(User user, Studio studio, String title, String description, String imageUrl, 
                   String hashtags, Boolean isPublic, ArtworkStatus status,
                   String shootingDate, String shootingLocation) {
        this.user = user;
        this.studio = studio;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.hashtags = hashtags;
        this.isPublic = isPublic != null ? isPublic : true;
        this.status = status != null ? status : ArtworkStatus.PUBLIC;
        this.shootingDate = shootingDate;               // [추가]
        this.shootingLocation = shootingLocation;       // [추가]
    }
    
    public void updateBasicInfo(String title, String description, String hashtags) {
        this.title = title;
        this.description = description;
        this.hashtags = hashtags;
    }
    
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void changeVisibility(Boolean isPublic) {
        this.isPublic = isPublic;
        if (!isPublic) {
            this.status = ArtworkStatus.PRIVATE;
        } else {
            this.status = ArtworkStatus.PUBLIC;
        }
    }
    
    public void changeStatus(ArtworkStatus status) {
        this.status = status;
    }
    
    public void makePublic() {
        this.isPublic = true;
        this.status = ArtworkStatus.PUBLIC;
    }
    
    public void makePrivate() {
        this.isPublic = false;
        this.status = ArtworkStatus.PRIVATE;
    }
    
    public void report() {
        this.status = ArtworkStatus.REPORTED;
    }
    
    public boolean isPublic() {
        return this.isPublic && this.status == ArtworkStatus.PUBLIC;
    }
    
    public boolean isPrivate() {
        return !this.isPublic || this.status == ArtworkStatus.PRIVATE;
    }
    
    public boolean isReported() {
        return this.status == ArtworkStatus.REPORTED;
    }

    // 순서 변경을 위한 도메인 메서드
    public void changeOrder(Integer newOrder) {
        if (newOrder != null && newOrder >= 0) {
            this.order = newOrder;
        } else {
            throw new IllegalArgumentException("순서는 0 이상의 숫자여야 합니다.");
        }

    }

    public void update(String title, String description, String hashtags,
                       String shootingDate, String shootingLocation, String imageUrl, Boolean isPublic) {
        this.title = title;
        this.description = description;
        this.hashtags = hashtags;
        this.shootingDate = shootingDate;
        this.shootingLocation = shootingLocation;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic != null && isPublic;
    }
}
