package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.artwork.AdminArtWorkDTOs.*;

import java.util.List;

/**
 * 관리자 작품 관리 서비스 인터페이스
 */
public interface AdminArtWorkService {

    /**
     * 작품 목록 조회 (페이징, 필터링)
     */
    AdminArtWorkListResponse getArtWorks(int page, int size, String status, String keyword);

    /**
     * 작품 상세 조회
     */
    AdminArtWorkDetailResponse getArtWorkDetail(Long artworkId);

    /**
     * 작품 상태 변경 (예: 공개, 비공개, 신고됨)
     */
    AdminArtWorkStatusResponse changeArtWorkStatus(Long artworkId, AdminArtWorkStatusCommand command);

    /**
     * 작품 삭제
     */
    void deleteArtWork(Long artworkId, String reason);

    /**
     * 작품 통계 조회
     */
    AdminArtWorkStatsResponse getArtWorkStats();

    /**
     * 인기 작품 조회
     */
    AdminPopularArtWorkResponse getPopularArtWorks(String period, int limit);

    /**
     * 신고된 작품 목록 조회
     */
    List<ReportedArtWorkDto> getReportedArtWorks(int page, int size);

    /**
     * 작품 생성 (관리자용)
     */
    Long createArtWork(AdminArtWorkCreateCommand command, Long adminUserId);

    /**
     * 작품 좋아요 추가
     */
    void likeArtWork(Long artworkId, Long userId);

    /**
     * 작품 좋아요 취소
     */
    void unlikeArtWork(Long artworkId, Long userId);

    /**
     * 작품 댓글 목록 조회
     */
    List<AdminArtWorkCommentDto> getComments(Long artworkId, int page, int size);

    /**
     * 작품 댓글 삭제
     */
    void deleteComment(Long commentId, String reason);

    /**
     * 작품 댓글 생성 (관리자 생성)
     */
    Long createComment(AdminArtWorkCommentCreateCommand command, Long adminUserId);
}
