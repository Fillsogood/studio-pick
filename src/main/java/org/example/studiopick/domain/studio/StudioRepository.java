package org.example.studiopick.domain.studio;

import jakarta.persistence.LockModeType;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StudioRepository {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM Studio s WHERE s.id = :id")
  public Optional<Studio> findByIdWithLock(@Param("id") Long id);

  //관리자용
  Page<Studio> findAllByOrderByCreatedAtDesc(Pageable pageable);
  Page<Studio> findByStatusOrderByCreatedAtDesc(StudioStatus status, Pageable pageable);
  Page<Studio> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name, Pageable pageable);
  Page<Studio> findByStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(StudioStatus status, String name, Pageable pageable);
  boolean existsByName(String name);
  long countByStatus(StudioStatus status);
}
