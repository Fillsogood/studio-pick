package org.example.studiopick.application.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationAuthServiceImpl implements OperationAuthService {
    
    private final JpaStudioRepository studioRepository;

    /**
     * 공간 대여 권한 + 소유권 + 상태 검증
     */
    public void validateSpaceRentalPermission(Long studioId, Long userId) {
        Studio studio = studioRepository.findById(studioId)
            .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));
            
        // 1. 소유권 확인
        if (!studio.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("해당 스튜디오에 대한 권한이 없습니다.");
        }
        
        // 2. 공간 대여 타입 확인
        if (!studio.isSpaceRental()) {
            throw new IllegalArgumentException("공간 대여 스튜디오가 아닙니다.");
        }
        
        // 3. 승인 상태 확인
        if (!studio.isActive()) {
            throw new IllegalStateException("승인되지 않은 스튜디오입니다. 현재 상태: " + studio.getStatus());
        }
        
        // 4. 사용자 권한 확인
        User owner = studio.getOwner();
        if (!owner.isStudioOwner()) {
            throw new AccessDeniedException("스튜디오 운영 권한이 없습니다.");
        }
    }

    /**
     * 공방 체험 권한 + 소유권 + 상태 검증
     */
    public void validateClassWorkshopPermission(Long studioId, Long userId) {
        Studio studio = studioRepository.findById(studioId)
            .orElseThrow(() -> new IllegalArgumentException("공방을 찾을 수 없습니다."));
            
        // 1. 소유권 확인
        if (!studio.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("해당 공방에 대한 권한이 없습니다.");
        }
        
        // 2. 공방 체험 타입 확인
        if (!studio.isClassWorkshop()) {
            throw new IllegalArgumentException("공방 체험 스튜디오가 아닙니다.");
        }
        
        // 3. 승인 상태 확인
        if (!studio.isActive()) {
            throw new IllegalStateException("승인되지 않은 공방입니다. 현재 상태: " + studio.getStatus());
        }
        
        // 4. 사용자 권한 확인
        User owner = studio.getOwner();
        if (!owner.isStudioOwner()) {
            throw new AccessDeniedException("공방 운영 권한이 없습니다.");
        }
    }
}
