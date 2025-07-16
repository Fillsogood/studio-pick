package org.example.studiopick.application.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


public interface OperationAuthService {
    

    void validateSpaceRentalPermission(Long studioId, Long userId);


    void validateClassWorkshopPermission(Long studioId, Long userId);
}
