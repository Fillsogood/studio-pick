package org.example.studiopick.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByStudioId(Long studioId);
}