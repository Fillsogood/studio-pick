package org.example.studiopick.infrastructure.studio;

import org.example.studiopick.domain.studio.Studio;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface StudioRepository extends JpaRepository<Studio, Long>, JpaSpecificationExecutor<Studio> {
  Page<Studio> findAll(Pageable pageable);

  @Query("SELECT s FROM Studio s " +
      "WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
      "AND (:location IS NULL OR s.location LIKE CONCAT('%', :location, '%'))")
  List<Studio> searchStudios(
      @Param("keyword") String keyword,
      @Param("location") String location
  );
}