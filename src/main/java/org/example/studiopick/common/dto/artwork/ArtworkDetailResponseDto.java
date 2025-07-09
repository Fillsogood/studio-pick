package org.example.studiopick.common.dto.artwork;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ArtworkDetailResponseDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;

    private SimpleUserDto user; // 작성자
    private SimpleStudioDto studio; // 소속 스튜디오

    private int likeCount;
    private boolean isLiked;

    private List<ArtworkCommentResponseDto> comments;  // 댓글 목록
}
