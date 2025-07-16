package org.example.studiopick.infrastructure.workshop;

import org.example.studiopick.domain.workshop.WorkShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaWorkShopRepository extends JpaRepository<WorkShop, Long> {
  List<WorkShop> findByStudioId_IdAndStatusAndDate(LocalDate date);

}
