package org.example.studiopick.domain.artwork;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtworkLikeService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkLikeRepository artworkLikeRepository;

    @Transactional
    public void toggleLike(Long artworkId, User user) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 존재하지 않습니다."));

        boolean alreadyLiked = artworkLikeRepository.existsByArtworkAndUser(artwork, user);

        if (alreadyLiked) {
            artworkLikeRepository.deleteByArtworkAndUser(artwork, user);
        } else {
            ArtworkLike like = ArtworkLike.builder()
                    .artwork(artwork)
                    .user(user)
                    .build();
            artworkLikeRepository.save(like);
        }
    }
}
