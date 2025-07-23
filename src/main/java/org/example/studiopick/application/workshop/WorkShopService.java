package org.example.studiopick.application.workshop;

import org.example.studiopick.application.workshop.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 워크샵(공방) 관련 비즈니스 로직 인터페이스
 */
public interface WorkShopService {

    /** 공방 목록 조회 */
    WorkShopListResponse getWorkShopList(String status, String date);

    /** 공방 상세 조회 */
    WorkShopDetailDto getWorkShopDetail(Long workshopId);

    /** 공방 운영 신청 */
    WorkShopApplicationResponse applyWorkshop(WorkShopApplicationRequest request, Long userId);

    /** 공방 신청 상태 조회 */
    WorkShopApplicationDetailResponse getWorkshopApplicationStatus(Long workshopId);

    /** 호스트용 클래스 관리 리스트 조회 */
    List<ClassManageItemResponseDto> getClassManageList(Long ownerUserId);

    /** 워크샵 상태를 ACTIVE/INACTIVE 으로 변경 */
    void updateWorkshopStatus(Long workshopId, String status);

    /** 공방 정보 수정 */
    WorkShopDetailDto updateWorkshop(
      Long workshopId,
      WorkShopUpdateRequestDto request,
      Long ownerUserId
    );

    /** 공방 비활성화 (별도 hideStatus 플래그 없이 도메인 메서드로 처리) */
    void deactivateWorkshop(Long workshopId);

    /** 승인 후 공방 활성화 및 생성 처리 (관리자용) */
    Long activateAndCreateWorkshop(Long workshopApplicationId, WorkShopCreateCommand command, Long adminUserId);

    /** 강의(클래스) 이미지 업로드 */
    List<String> uploadClassImages(List<MultipartFile> files);

    /** 강의(클래스) 이미지 삭제 */
    void deleteClassImages(List<String> imageUrls);
}
