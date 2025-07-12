package org.example.studiopick.common.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.studiopick.domain.favorite.Favorite;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class FavoriteResponseDto {

    private Long id;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String targetLocation;
    private LocalDateTime createdAt;

    // DTO에서는 로직 최소화 → 데이터만 담는 역할
}
