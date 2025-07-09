package org.example.studiopick.infrastructure.classes;

import org.example.studiopick.domain.class_entity.ClassReservation;
import org.example.studiopick.domain.common.enums.ClassReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassReservationRepository extends JpaRepository<ClassReservation, Long> {
  List<ClassReservation> findByUserIdAndStatus(Long userId, ClassReservationStatus status);
}
