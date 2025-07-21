package org.example.studiopick.infrastructure.studio;

import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.studio.Studio;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaStudioRepository extends JpaRepository<Studio, Long>, JpaSpecificationExecutor<Studio> {
  Page<Studio> findAll(Pageable pageable);

  @Query("SELECT s FROM Studio s " +
      "WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
      "AND (:location IS NULL OR s.location LIKE CONCAT('%', :location, '%'))")
  List<Studio> searchStudios(
      @Param("keyword") String keyword,
      @Param("location") String location
  );

  // 활성화된 스튜디오만 조회
  @Query("SELECT s FROM Studio s WHERE s.status = 'ACTIVE'")
  Page<Studio> findActiveStudios(Pageable pageable);


  //관리자용
  Page<Studio> findAllByOrderByCreatedAtDesc(Pageable pageable);
  Page<Studio> findByStatusOrderByCreatedAtDesc(StudioStatus status, Pageable pageable);
  Page<Studio> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name, Pageable pageable);
  Page<Studio> findByStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(StudioStatus status, String name, Pageable pageable);
  boolean existsByName(String name);
  long countByStatus(StudioStatus status);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  List<Studio> findAllByStatus(StudioStatus status);

  @Query("SELECT s FROM Studio s WHERE s.owner.id = :ownerUserId AND s.status IN ('APPROVED', 'ACTIVE')")
  List<Studio> findByOwnerId(@Param("ownerUserId") Long userId);

}
