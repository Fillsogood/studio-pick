package org.example.studiopick.infrastructure.studio;

import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.StudioRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStudioRepository extends JpaRepository<Studio, Long>, StudioRepository {


}
