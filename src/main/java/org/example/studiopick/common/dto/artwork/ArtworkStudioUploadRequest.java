package org.example.studiopick.common.dto.artwork;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ArtworkStudioUploadRequest {

    @Schema(description = "작품 제목")
    private String title;

    @Schema(description = "작품 설명")
    private String description;

    @Schema(description = "스튜디오 ID")
    private Long studioId;

    @Schema(description = "해시태그")
    private String hashtags;

    @Schema(description = "공개 여부")
    private boolean isPublic;

    @Schema(description = "대표 이미지 파일", type = "string", format = "binary")
    private MultipartFile image;
}
