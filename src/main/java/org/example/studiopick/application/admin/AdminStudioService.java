package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.studio.*;

/**
 * 관리자 스튜디오 관리 서비스 인터페이스
 */
public interface AdminStudioService {

    /**
     * 스튜디오 계정 목록 조회 (페이징, 필터링)
     */
    AdminStudioListResponse getStudioAccounts(int page, Integer size, String status, String keyword);

    /**
     * 스튜디오 계정 상세 조회
     */
    AdminStudioDetailResponse getStudioAccount(Long studioId);

    /**
     * 스튜디오 계정 생성
     */
    AdminStudioCreateResponse createStudioAccount(AdminStudioCreateCommand command);

    /**
     * 스튜디오 계정 수정
     */
    AdminStudioUpdateResponse updateStudioAccount(Long studioId, AdminStudioUpdateCommand command);

    /**
     * 스튜디오 계정 상태 변경 (승인/거부/정지/활성화)
     */
    AdminStudioStatusResponse changeStudioStatus(Long studioId, AdminStudioStatusCommand command);

    /**
     * 스튜디오 계정 삭제 (소프트 삭제)
     */
    void deleteStudioAccount(Long studioId, String reason);

    /**
     * 스튜디오 통계 조회
     */
    AdminStudioStatsResponse getStudioStats();
}
