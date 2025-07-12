package org.example.studiopick.domain.favorite;

import org.example.studiopick.domain.common.enums.FavoriteType;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndTargetTypeAndTargetId(User user, FavoriteType targetType, Long targetId);

    Page<Favorite> findByUserAndTargetType(User user, FavoriteType targetType, Pageable pageable);
}
