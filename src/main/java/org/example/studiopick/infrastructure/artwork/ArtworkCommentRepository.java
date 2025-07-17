package org.example.studiopick.infrastructure.artwork;

import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.artwork.ArtworkComment;
import org.example.studiopick.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtworkCommentRepository extends JpaRepository<ArtworkComment, Long> {
    List<ArtworkComment> findByArtwork(Artwork artwork);

    Page<ArtworkComment> findByArtworkId(Long artworkId, Pageable pageable);

}
