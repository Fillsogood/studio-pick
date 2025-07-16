package org.example.studiopick.infrastructure.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ReportRepository extends JpaRepository<Report, Long> {

    // 중복 신고 확인
    boolean existsByUserAndReportedTypeAndReportedId(User user, ReportType reportedType, Long reportedId);
    
    // 특정 상태의 신고 횟수 조회
    long countByReportedTypeAndReportedIdAndStatus(ReportType reportedType, Long reportedId, ReportStatus status);
    
    // 총 신고 횟수 조회 (모든 상태 포함)
    long countByReportedTypeAndReportedId(ReportType reportedType, Long reportedId);
    
    // 특정 컨텐츠에 대한 모든 신고 조회
    List<Report> findByReportedTypeAndReportedIdOrderByCreatedAtDesc(ReportType reportedType, Long reportedId);

    
    // 관리자용: 자동 비공개 처리된 신고 목록 조회
    @Query("SELECT r FROM Report r WHERE r.status = 'AUTO_HIDDEN' ORDER BY r.processedAt DESC")
    List<Report> findAutoHiddenReports();
    
    // 관리자용: 특정 기간 내 신고 통계 조회
    @Query("SELECT r.reportedType, COUNT(r) FROM Report r WHERE r.createdAt >= :startDate GROUP BY r.reportedType")
    List<Object[]> getReportStatistics(@Param("startDate") java.time.LocalDateTime startDate);
    
    // 상태별 카운트 조회
    long countByStatus(ReportStatus status);
    
    // 특정 상태가 아닌 신고 카운트
    long countByStatusNot(ReportStatus status);
    
    // 사용자별 신고 카운트
    long countByUser(User user);
    
    // 상태별 페이징 조회
    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    Page<Report> findByStatusOrderByCreatedAtDesc(@Param("status") ReportStatus status, Pageable pageable);
    
    // 타입별 페이징 조회
    @Query("SELECT r FROM Report r WHERE r.reportedType = :type ORDER BY r.createdAt DESC")
    Page<Report> findByReportedTypeOrderByCreatedAtDesc(@Param("type") ReportType type, Pageable pageable);
}
