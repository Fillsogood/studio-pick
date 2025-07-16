package org.example.studiopick.application.artwork;

import org.example.studiopick.domain.user.User;

/**
 * 작품 좋아요 관리 서비스 인터페이스
 */
public interface ArtworkLikeService {

    /**
     * 좋아요 토글 (좋아요/좋아요 취소)
     */
    void toggleLike(Long artworkId, User user);
}
