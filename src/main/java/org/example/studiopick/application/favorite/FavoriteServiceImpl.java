package org.example.studiopick.application.favorite;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.favorite.FavoriteCreateDto;
import org.example.studiopick.common.favorite.FavoriteResponseDto;
import org.example.studiopick.domain.common.enums.FavoriteType;
import org.example.studiopick.domain.favorite.Favorite;
import org.example.studiopick.infrastructure.favorite.JpaFavoriteRepository;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final JpaFavoriteRepository jpaFavoriteRepository;
    private final JpaStudioRepository studioRepository;

    @Override
    public FavoriteResponseDto addFavorite(User user, FavoriteCreateDto dto) {
        FavoriteType type = FavoriteType.from(dto.getTargetType());

        Optional<Favorite> existing = jpaFavoriteRepository.findByUserAndTargetTypeAndTargetId(user, type, dto.getTargetId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 항목입니다.");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .targetType(type)
                .targetId(dto.getTargetId())
                .build();

        Favorite saved = jpaFavoriteRepository.save(favorite);
        return convertToDto(saved);
    }

    @Override
    public void deleteFavorite(User user, Long favoriteId) {
        Favorite favorite = jpaFavoriteRepository.findById(favoriteId)
                .filter(f -> f.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("해당 즐겨찾기를 찾을 수 없습니다."));

        jpaFavoriteRepository.delete(favorite);
    }

    @Override
    public Page<FavoriteResponseDto> getFavorites(User user, String type, Pageable pageable) {
        FavoriteType favoriteType = FavoriteType.from(type);

        return jpaFavoriteRepository.findByUserAndTargetType(user, favoriteType, pageable)
                .map(this::convertToDto); // 여기서 studioRepository 접근 포함
    }

    private FavoriteResponseDto convertToDto(Favorite favorite) {
        String targetName = null;
        String targetLocation = null;

        if (favorite.getTargetType() == FavoriteType.STUDIO) {
            Studio studio = studioRepository.findById(favorite.getTargetId())
                    .orElseThrow(() -> new RuntimeException("스튜디오 없음"));
            targetName = studio.getName();
            targetLocation = studio.getLocation(); // address로 바꿔도 됨
        }

        return FavoriteResponseDto.builder()
                .id(favorite.getId())
                .targetType(favorite.getTargetType().getValue())
                .targetId(favorite.getTargetId())
                .targetName(targetName)
                .targetLocation(targetLocation)
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
