package org.example.studiopick.infrastructure.studio;

import org.example.studiopick.domain.common.enums.Weekday;
import org.example.studiopick.domain.studio.StudioOperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaStudioOperatingHoursRepository extends JpaRepository<StudioOperatingHours, Long> {
  List<StudioOperatingHours> findByStudioId(Long studioId);
  void deleteByStudioId(Long studioId);
  List<StudioOperatingHours> findByWeekday(Weekday weekday);
}