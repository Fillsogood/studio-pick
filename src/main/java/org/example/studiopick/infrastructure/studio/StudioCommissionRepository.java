package org.example.studiopick.infrastructure.studio;

import org.example.studiopick.domain.studio.StudioCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudioCommissionRepository extends JpaRepository<StudioCommission, Long> {}

