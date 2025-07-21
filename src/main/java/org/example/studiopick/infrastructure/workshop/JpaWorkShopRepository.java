package org.example.studiopick.infrastructure.workshop;

import org.example.studiopick.domain.common.enums.WorkShopStatus;
import org.example.studiopick.domain.workshop.WorkShop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaWorkShopRepository extends JpaRepository<WorkShop, Long> {

  // 상태 + 날짜 기반 목록 조회
  List<WorkShop> findByStatusAndDate(WorkShopStatus status, LocalDate date);

  // 상태 기반 페이지 조회
  Page<WorkShop> findByStatus(WorkShopStatus status, Pageable pageable);

  List<WorkShop> findByStatus(WorkShopStatus status);

  // 상태 + 제목 기반 필터링
  Page<WorkShop> findByStatusAndTitleContaining(WorkShopStatus status, String title, Pageable pageable);

  // 제목 검색
  Page<WorkShop> findByTitleContaining(String title, Pageable pageable);

  // 상태별 개수 카운트
  long countByStatus(WorkShopStatus status);

  // 최신순 정렬 조회 (인기 공방용)
  List<WorkShop> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
