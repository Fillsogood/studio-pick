package org.example.studiopick.infrastructure.artwork;

import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.common.enums.ArtworkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    Optional<Artwork> findByIdAndUserId(Long artworkId, Long userId);
    long countByHideStatus(ArtworkStatus status);

    List<Artwork> findAllByOrderByLikeCountDesc(Pageable pageable);

    Page<Artwork> findByHideStatus(ArtworkStatus status, Pageable pageable);

    @Query("SELECT COALESCE(AVG(a.likeCount), 0) FROM Artwork a")
    Optional<BigDecimal> calculateAverageLikeCount();


}
