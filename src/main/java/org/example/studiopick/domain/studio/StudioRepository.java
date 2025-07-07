package org.example.studiopick.domain.studio;

import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StudioRepository {
  public Optional<Studio> findById(Long id);

  public Optional<Studio> findByIdWithLock(@Param("id") Long id);
}
