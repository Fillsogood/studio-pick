package org.example.studiopick.application.artwork;

import org.example.studiopick.common.dto.artwork.ArtworkCommentResponseDto;
import org.example.studiopick.domain.user.User;

import java.util.List;

/**
 * 작품 댓글 관리 서비스 인터페이스
 */
public interface ArtworkCommentService {

    /**
     * 댓글 생성
     */
    ArtworkCommentResponseDto createComment(Long artworkId, User user, String commentText);

    /**
     * 작품별 댓글 목록 조회
     */
    List<ArtworkCommentResponseDto> getCommentsByArtworkId(Long artworkId);

    /**
     * 댓글 수정
     */
    ArtworkCommentResponseDto updateComment(Long artworkId, Long commentId, User user, String newComment);

    /**
     * 댓글 삭제
     */
    void deleteComment(Long artworkId, Long commentId, User user);
}
