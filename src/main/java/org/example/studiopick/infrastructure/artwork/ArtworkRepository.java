package org.example.studiopick.infrastructure.artwork;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.artwork.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
  List<Artwork> findByStudioId(Long studioId);
}
