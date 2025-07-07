package org.example.studiopick.infrastructure.studio;

import org.example.studiopick.domain.studio.StudioOperatingHours;
import org.example.studiopick.domain.studio.StudioOperatingHoursRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStudioOperatingHoursRepository extends JpaRepository<StudioOperatingHours, Long> , StudioOperatingHoursRepository {
}
