package org.example.studiopick.application.artwork;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.dto.artwork.ArtworkCommentResponseDto;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.artwork.ArtworkComment;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.artwork.ArtworkCommentRepository;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtworkCommentServiceImpl implements ArtworkCommentService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkCommentRepository artworkCommentRepository;

    @Override
    @Transactional
    public ArtworkCommentResponseDto createComment(Long artworkId, User user, String commentText) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 존재하지 않습니다."));

        ArtworkComment comment = ArtworkComment.builder()
                .artwork(artwork)
                .user(user)
                .comment(commentText)
                .createdAt(LocalDateTime.now())
                .build();

        ArtworkComment saved = artworkCommentRepository.save(comment);

        return ArtworkCommentResponseDto.builder()
                .id(saved.getId())
                .comment(saved.getComment())
                .nickname(saved.getUser().getNickname())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtworkCommentResponseDto> getCommentsByArtworkId(Long artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 존재하지 않습니다."));

        return artworkCommentRepository.findByArtwork(artwork).stream()
                .map(comment -> ArtworkCommentResponseDto.builder()
                        .id(comment.getId())
                        .comment(comment.getComment())
                        .nickname(comment.getUser().getNickname())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ArtworkCommentResponseDto updateComment(Long artworkId, Long commentId, User user, String newComment) {
        ArtworkComment comment = artworkCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getArtwork().getId().equals(artworkId)) {
            throw new IllegalArgumentException("댓글이 해당 작품에 속하지 않습니다.");
        }

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 댓글만 수정할 수 있습니다.");
        }

        comment.updateComment(newComment); // setter 대신 update 메서드 활용
        return ArtworkCommentResponseDto.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .nickname(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteComment(Long artworkId, Long commentId, User user) {
        ArtworkComment comment = artworkCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getArtwork().getId().equals(artworkId)) {
            throw new IllegalArgumentException("댓글이 해당 작품에 속하지 않습니다.");
        }

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 댓글만 삭제할 수 있습니다.");
        }

        artworkCommentRepository.delete(comment);
    }
}
