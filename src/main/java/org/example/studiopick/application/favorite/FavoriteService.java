package org.example.studiopick.application.favorite;

import org.example.studiopick.common.favorite.FavoriteCreateDto;
import org.example.studiopick.common.favorite.FavoriteResponseDto;
import org.example.studiopick.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 즐겨찾기 관리 서비스 인터페이스
 */
public interface FavoriteService {

    /**
     * 즐겨찾기 추가
     */
    FavoriteResponseDto addFavorite(User user, FavoriteCreateDto dto);

    /**
     * 즐겨찾기 삭제
     */
    void deleteFavorite(User user, Long favoriteId);

    /**
     * 사용자별 즐겨찾기 목록 조회
     */
    Page<FavoriteResponseDto> getFavorites(User user, String type, Pageable pageable);
}
