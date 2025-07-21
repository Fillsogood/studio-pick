package org.example.studiopick.infrastructure.studio;

import org.example.studiopick.domain.studio.StudioImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStudioImageRepository extends JpaRepository<StudioImage, Long> {
}
