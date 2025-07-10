package org.example.studiopick.common.dto.artwork;

import lombok.Data;

import java.util.List;

@Data
public class ArtworkUploadRequestDto {
    private String title;
    private String description;
    private String hashtags;
    private String shootingDate;
    private String shootingLocation;
    private Long studioId;
    private Boolean isPublic;
    private List<String> imageUrls;
}
