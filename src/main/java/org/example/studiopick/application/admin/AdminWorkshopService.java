package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.workshop.WorkshopDTOs.*;

import java.util.List;


/**
 * 관리자 워크샵 관리 서비스 인터페이스
 */
public interface AdminWorkshopService {

    /**
     * 워크샵 목록 조회 (페이징, 필터링)
     */
    AdminWorkshopListResponse getWorkshops(int page, Integer size, String status, String keyword);

    /**
     * 워크샵 상세 조회
     */
    AdminWorkshopDetailResponse getWorkshopDetail(Long workshopId);

    /**
     * 워크샵 승인/거부
     */
    AdminWorkshopApprovalResponse approveWorkshop(Long workshopId, AdminWorkshopApprovalCommand command);

    /**
     * 워크샵 상태 변경
     */
    AdminWorkshopStatusResponse changeWorkshopStatus(Long workshopId, AdminWorkshopStatusCommand command);

    /**
     * 워크샵 삭제
     */
    void deleteWorkshop(Long workshopId, String reason);

    /**
     * 워크샵 통계 조회
     */
    AdminWorkshopStatsResponse getWorkshopStats();


    /**
     * 인기 워크샵 조회
     */
    AdminPopularWorkshopResponse getPopularWorkshops(String period, int limit);

    /**
     * 신고된 공방 목록 조회
     */
    List<ReportedWorkshopDto> getReportedWorkshops(int page, Integer size);

}
