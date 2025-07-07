package org.example.studiopick.infrastructure.studio;

import jakarta.persistence.LockModeType;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.StudioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaStudioRepository extends JpaRepository<Studio, Long>, StudioRepository {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM Studio s WHERE s.id = :id")
  Optional<Studio> findByIdWithLock(@Param("id") Long id);
}
