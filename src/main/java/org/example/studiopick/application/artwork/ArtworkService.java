package org.example.studiopick.application.artwork;

import org.example.studiopick.common.dto.artwork.*;
import org.example.studiopick.domain.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 작품 관리 서비스 인터페이스
 */
public interface ArtworkService {

    /**
     * 작품 피드 조회
     */
    List<ArtworkFeedDto> getArtworks(String sort, int offset, int limit, String hashtags);

    /**
     * 작품 상세 조회
     */
    ArtworkDetailResponseDto getArtworkDetail(Long artworkId, User user);

    /**
     * S3 업로드 기능
     */
    String uploadToS3(MultipartFile file);

    /**
     * 작품 저장
     */
    Long saveArtwork(ArtworkUploadRequestDto dto, Long userId);

    /**
     * 작품 수정
     */
    void updateArtwork(Long artworkId, Long userId, ArtworkUploadRequestDto dto);

    /**
     * 작품 삭제
     */
    void deleteArtwork(Long artworkId, Long userId);
}
