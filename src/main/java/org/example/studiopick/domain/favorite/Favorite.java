package org.example.studiopick.domain.favorite;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.FavoriteType;
import org.example.studiopick.domain.user.User;

@Entity
@Table(name = "\"Favorite\"", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_type", "target_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private FavoriteType targetType;
    
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    @Builder
    public Favorite(User user, FavoriteType targetType, Long targetId) {
        this.user = user;
        this.targetType = targetType;
        this.targetId = targetId;
    }
    
    public void updateTarget(FavoriteType targetType, Long targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }
    
    public boolean isStudioFavorite() {
        return this.targetType == FavoriteType.STUDIO;
    }
    
    public boolean isClassFavorite() {
        return this.targetType == FavoriteType.CLASS;
    }
}
