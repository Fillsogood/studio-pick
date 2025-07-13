package org.example.studiopick.infrastructure.studio;

import jakarta.persistence.LockModeType;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.common.enums.OperationType;
import org.example.studiopick.domain.studio.Studio;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

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

  Optional<Studio> findByIdAndStatus(Long id, StudioStatus status);

  //관리자용
  Page<Studio> findAllByOrderByCreatedAtDesc(Pageable pageable);
  Page<Studio> findByStatusOrderByCreatedAtDesc(StudioStatus status, Pageable pageable);
  Page<Studio> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name, Pageable pageable);
  Page<Studio> findByStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(StudioStatus status, String name, Pageable pageable);
  boolean existsByName(String name);
  long countByStatus(StudioStatus status);
  
  // 운영 타입별 메서드들
  boolean existsByOwnerIdAndOperationTypeAndStatusIn(Long ownerId, OperationType operationType, List<StudioStatus> statuses);
  List<Studio> findByOperationType(OperationType operationType);
  Page<Studio> findByOperationTypeAndStatusOrderByCreatedAtDesc(OperationType operationType, StudioStatus status, Pageable pageable);
  Page<Studio> findByOperationTypeOrderByCreatedAtDesc(OperationType operationType, Pageable pageable);
  Page<Studio> findByOperationTypeAndStatusAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
      OperationType operationType, StudioStatus status, String name, Pageable pageable);
  long countByOperationType(OperationType operationType);
  long countByOperationTypeAndStatus(OperationType operationType, StudioStatus status);

}