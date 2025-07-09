package org.example.studiopick.domain.artwork;

import org.example.studiopick.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtworkLikeRepository extends JpaRepository<ArtworkLike, Long> {
    boolean existsByArtworkAndUser(Artwork artwork, User user);
    int countByArtwork(Artwork artwork);
    void deleteByArtworkAndUser(Artwork artwork, User user);
}
