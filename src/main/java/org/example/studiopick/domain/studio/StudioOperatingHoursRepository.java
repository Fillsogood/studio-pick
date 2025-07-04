package org.example.studiopick.domain.studio;

import java.time.DayOfWeek;
import java.util.Optional;

public interface StudioOperatingHoursRepository {

  Optional<StudioOperatingHours> findByStudioIdAndWeekday(Long studioId, DayOfWeek dayOfWeek);
}
