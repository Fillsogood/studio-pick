package org.example.studiopick.domain.studio.repository;

import org.example.studiopick.domain.class_entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardClassRepository extends JpaRepository<ClassEntity, Long> {

    long countByStudioId(Long studioId);
}
