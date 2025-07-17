package org.example.studiopick.infrastructure.workshop;

import org.example.studiopick.domain.common.enums.HideStatus;
import org.example.studiopick.domain.workshop.WorkShop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaWorkShopRepository extends JpaRepository<WorkShop, Long> {
  List<WorkShop> findByHideStatusAndDate(HideStatus hideStatus, LocalDate date);
  Page<WorkShop> findByHideStatus(HideStatus hideStatus, Pageable pageable);

  Page<WorkShop> findByHideStatusAndTitleContaining(HideStatus hideStatus, String title, Pageable pageable);

  Page<WorkShop> findByTitleContaining(String title, Pageable pageable);
  long countByHideStatus(HideStatus hideStatus);

  List<WorkShop> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
