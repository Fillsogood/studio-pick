package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.user.*;

/**
 * 관리자 사용자 관리 서비스 인터페이스
 */
public interface AdminUserService {

    /**
     * 사용자 계정 목록 조회 (페이징, 필터링)
     */
    AdminUserListResponse getUserAccounts(int page, Integer size, String role, String status, String keyword);

    /**
     * 사용자 계정 상세 조회
     */
    AdminUserDetailResponse getUserAccount(Long userId);

    /**
     * 사용자 계정 생성
     */
    AdminUserCreateResponse createUserAccount(AdminUserCreateCommand command);

    /**
     * 사용자 계정 수정
     */
    AdminUserUpdateResponse updateUserAccount(Long userId, AdminUserUpdateCommand command);

    /**
     * 사용자 계정 상태 변경 (활성화/비활성화/잠금)
     */
    AdminUserStatusResponse changeUserStatus(Long userId, AdminUserStatusCommand command);

    /**
     * 사용자 역할 변경 (USER ↔ STUDIO_OWNER)
     */
    AdminUserRoleResponse changeUserRole(Long userId, AdminUserRoleCommand command);

    /**
     * 사용자 계정 삭제 (소프트 삭제)
     */
    void deleteUserAccount(Long userId, String reason);

    /**
     * 사용자 통계 조회
     */
    AdminUserStatsResponse getUserStats();
}
