package org.example.studiopick.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.SocialProvider;

@Entity
@Table(name = "\"SocialAccount\"", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "provider"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private SocialProvider provider;
    
    @Column(name = "social_id", nullable = false, length = 50)
    private String socialId;
    
    @Builder
    public SocialAccount(User user, SocialProvider provider, String socialId) {
        this.user = user;
        this.provider = provider;
        this.socialId = socialId;
    }
    
    public void updateSocialId(String socialId) {
        this.socialId = socialId;
    }
}
