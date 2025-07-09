package org.example.studiopick.infrastructure.classes;

import org.example.studiopick.domain.class_entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
  List<ClassEntity> findByStudioIdAndStatusAndDate(Long studioId, Enum status, LocalDate date);
}
