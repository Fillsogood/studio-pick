package org.example.studiopick.domain.artwork;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.dto.artwork.*;
import org.example.studiopick.domain.common.enums.ArtworkStatus;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.example.studiopick.infrastructure.artwork.mybatis.ArtworkMapper;
import org.example.studiopick.infrastructure.s3.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtworkService {

    // ✅ S3 업로더
    private final S3Uploader s3Uploader;

    // ✅ 피드 정렬용 Mapper
    private final ArtworkMapper artworkMapper;

    // ✅ Repository들
    private final ArtworkRepository artworkRepository;
    private final ArtworkCommentRepository artworkCommentRepository;
    private final ArtworkLikeRepository artworkLikeRepository;

    // ✅ studio 저장소 → JpaStudioRepository로 변경 완료
    private final JpaStudioRepository studioRepository;

    private final UserRepository userRepository;

    // ✅ 작품 피드 조회
    public List<ArtworkFeedDto> getArtworks(String sort, int offset, int limit, String hashtags) {
        return artworkMapper.findAllSorted(sort, offset, limit, hashtags);
    }

    // ✅ 작품 상세 조회
    public ArtworkDetailResponseDto getArtworkDetail(Long artworkId, User user) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 존재하지 않습니다."));

        List<ArtworkComment> commentEntities = artworkCommentRepository.findByArtwork(artwork);
        List<ArtworkCommentResponseDto> comments = commentEntities.stream()
                .map(c -> ArtworkCommentResponseDto.builder()
                        .id(c.getId())
                        .comment(c.getComment())
                        .nickname(c.getUser().getNickname())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        int likeCount = artworkLikeRepository.countByArtwork(artwork);
        boolean isLiked = artworkLikeRepository.existsByArtworkAndUser(artwork, user);

        return ArtworkDetailResponseDto.builder()
                .id(artwork.getId())
                .title(artwork.getTitle())
                .description(artwork.getDescription())
                .imageUrl(artwork.getImageUrl())
                .user(SimpleUserDto.builder()
                        .nickname(artwork.getUser().getNickname())
                        .build())
                .studio(SimpleStudioDto.builder()
                        .name(artwork.getStudio().getName())
                        .build())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .comments(comments)
                .build();
    }

    // ✅ S3 업로드 기능
    public String uploadToS3(MultipartFile file) {
        return s3Uploader.upload(file, "artworks");
    }

    // ✅ 작품 저장 기능
    public Long saveArtwork(ArtworkUploadRequestDto dto, Long userId) {
        Studio studio = studioRepository.findById(dto.getStudioId())
                .orElseThrow(() -> new IllegalArgumentException("해당 스튜디오가 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        Artwork artwork = Artwork.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .hashtags(dto.getHashtags())
                .shootingDate(dto.getShootingDate())
                .shootingLocation(dto.getShootingLocation())
                .studio(studio)
                .user(user)
                .imageUrl(dto.getImageUrls().get(0)) // 대표 이미지
                .isPublic(dto.getIsPublic() != null && dto.getIsPublic())
                .status(ArtworkStatus.PUBLIC)
                .build();

        artworkRepository.save(artwork);
        return artwork.getId();
    }

    // 작품 수정
    public void updateArtwork(Long artworkId, Long userId, ArtworkUploadRequestDto dto) {
        Artwork artwork = artworkRepository.findByIdAndUserId(artworkId, userId)
                .orElseThrow(() -> new IllegalArgumentException("작품을 수정할 권한이 없습니다."));

        artwork.update(
                dto.getTitle(),
                dto.getDescription(),
                dto.getHashtags(),
                dto.getShootingDate(),
                dto.getShootingLocation(),
                dto.getImageUrls() != null && !dto.getImageUrls().isEmpty() ? dto.getImageUrls().get(0) : artwork.getImageUrl(),
                dto.getIsPublic()
        );

        artworkRepository.save(artwork);
    }

    // 작품 삭제
    public void deleteArtwork(Long artworkId, Long userId) {
        Artwork artwork = artworkRepository.findByIdAndUserId(artworkId, userId)
                .orElseThrow(() -> new IllegalArgumentException("작품을 삭제할 권한이 없습니다."));

        artworkRepository.delete(artwork);
    }


}
