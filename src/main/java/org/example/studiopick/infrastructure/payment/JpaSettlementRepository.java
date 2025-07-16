package org.example.studiopick.infrastructure.payment;

import org.example.studiopick.domain.payment.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaSettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByStudioId(Long studioId);
}