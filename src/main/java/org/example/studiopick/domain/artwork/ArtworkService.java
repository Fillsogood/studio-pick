package org.example.studiopick.domain.artwork;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.dto.artwork.*;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.example.studiopick.infrastructure.mybatis.ArtworkMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtworkService {
    // Mapper (피드 정렬 조회용)
    private final ArtworkMapper artworkMapper;

    // Repository (상세조회용)
    private final ArtworkRepository artworkRepository;
    private final ArtworkCommentRepository artworkCommentRepository;
    private final ArtworkLikeRepository artworkLikeRepository;

    // 작품 피드 조회
    public List<ArtworkFeedDto> getArtworks(String sort, int offset, int limit, String hashtags) {
        return artworkMapper.findAllSorted(sort, offset, limit, hashtags);
    }

    // === 작품 상세 조회 ===
    public ArtworkDetailResponseDto getArtworkDetail(Long artworkId, User user) {
        // 1. 작품 가져오기
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 존재하지 않습니다."));

        // 2. 댓글 목록 가져오기
        List<ArtworkComment> commentEntities = artworkCommentRepository.findByArtwork(artwork);
        List<ArtworkCommentResponseDto> comments = commentEntities.stream()
                .map(c -> ArtworkCommentResponseDto.builder()
                        .id(c.getId())
                        .comment(c.getComment())
                        .nickname(c.getUser().getNickname())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        // 3. 좋아요 수 / 사용자 좋아요 여부
        int likeCount = artworkLikeRepository.countByArtwork(artwork);
        boolean isLiked = artworkLikeRepository.existsByArtworkAndUser(artwork, user);

        // 4. 응답 DTO 구성
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
}

