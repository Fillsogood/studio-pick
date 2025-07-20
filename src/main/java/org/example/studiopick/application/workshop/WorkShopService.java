package org.example.studiopick.application.workshop;

import org.example.studiopick.application.workshop.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WorkShopService {

    /**
     * 공방 목록 조회
     */
    WorkShopListResponse getWorkShopList(String status, String date);

    /**
     * 공방 상세 조회
     */
    WorkShopDetailDto getWorkShopDetail(Long workshopId);

    /**
     * 공방 운영 신청
     */
    WorkShopApplicationResponse applyWorkshop(WorkShopApplicationRequest request, Long userId);

    /**
     * 공방 신청 상태 조회
     */
    WorkShopApplicationDetailResponse getWorkshopApplicationStatus(Long workshopId);

    /**
     * 공방 정보 수정
     */
    void updateWorkshop(Long workshopId, WorkShopApplicationRequest request);

    /**
     * 공방 비활성화
     */
    void deactivateWorkshop(Long workshopId);

    /**
     * 승인 후 공방 활성화 및 생성 처리
     */
    Long activateAndCreateWorkshop(Long workshopApplicationId, WorkShopCreateCommand command, Long adminUserId);


    List<String> uploadClassImages(List<MultipartFile> files);

    void deleteClassImages(List<String> imageUrls);


}
