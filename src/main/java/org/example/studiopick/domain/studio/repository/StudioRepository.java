package org.example.studiopick.domain.studio.repository;

import org.example.studiopick.domain.studio.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRepository extends JpaRepository<Studio, Long> {
}
