package org.example.studiopick.infrastructure.artwork;

import org.example.studiopick.domain.artwork.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    Optional<Artwork> findByIdAndUserId(Long artworkId, Long userId);
}
