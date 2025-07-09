package org.example.studiopick.common.dto.artwork;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArtworkCommentResponseDto {
    private Long id;
    private String comment;
    private String nickname;
    private LocalDateTime createdAt;
}
