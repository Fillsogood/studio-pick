package org.example.studiopick.application.favorite;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.favorite.FavoriteCreateDto;
import org.example.studiopick.common.favorite.FavoriteResponseDto;
import org.example.studiopick.domain.common.enums.FavoriteType;
import org.example.studiopick.domain.favorite.Favorite;
import org.example.studiopick.domain.payment.FavoriteRepository;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.repository.StudioRepository;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final StudioRepository studioRepository;

    public FavoriteResponseDto addFavorite(User user, FavoriteCreateDto dto) {
        FavoriteType type = FavoriteType.from(dto.getTargetType());

        Optional<Favorite> existing = favoriteRepository.findByUserAndTargetTypeAndTargetId(user, type, dto.getTargetId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 항목입니다.");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .targetType(type)
                .targetId(dto.getTargetId())
                .build();

        Favorite saved = favoriteRepository.save(favorite);
        return convertToDto(saved);
    }

    public void deleteFavorite(User user, Long favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .filter(f -> f.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("해당 즐겨찾기를 찾을 수 없습니다."));

        favoriteRepository.delete(favorite);
    }

    public Page<FavoriteResponseDto> getFavorites(User user, String type, Pageable pageable) {
        FavoriteType favoriteType = FavoriteType.from(type);

        return favoriteRepository.findByUserAndTargetType(user, favoriteType, pageable)
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
