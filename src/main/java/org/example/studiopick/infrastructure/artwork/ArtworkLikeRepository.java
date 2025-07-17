package org.example.studiopick.infrastructure.artwork;

import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.artwork.ArtworkLike;
import org.example.studiopick.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtworkLikeRepository extends JpaRepository<ArtworkLike, Long> {
    boolean existsByArtworkAndUser(Artwork artwork, User user);
    int countByArtwork(Artwork artwork);
    void deleteByArtworkAndUser(Artwork artwork, User user);

    boolean existsByArtworkIdAndUserId(Long artworkId, Long userId);

    void deleteByArtworkIdAndUserId(Long artworkId, Long userId);

}
