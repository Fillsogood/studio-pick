package org.example.studiopick.infrastructure.artwork;

import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.artwork.ArtworkComment;
import org.example.studiopick.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtworkCommentRepository extends JpaRepository<ArtworkComment, Long> {
    List<ArtworkComment> findByArtwork(Artwork artwork);
    List<ArtworkComment> findByUser(User user);
}
