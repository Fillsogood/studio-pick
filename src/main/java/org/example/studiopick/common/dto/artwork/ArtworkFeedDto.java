package org.example.studiopick.common.dto.artwork;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor

public class ArtworkFeedDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl; //대표 이미지 url
    private String hashtags;
    private String artistNickname; // 작성자 닉네임
    private String studioName;
    private int likeCount; // 좋아요 수
    private boolean isPublic; // 공개여부
    private LocalDateTime createdAt;
}
