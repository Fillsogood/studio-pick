package org.example.studiopick.infrastructure.workshop;

import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.workshop.WorkShopImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWorkShopImageRepository extends JpaRepository<WorkShopImage, Long> {
  void deleteByWorkShop(WorkShop workshop);
}
