package org.example.studiopick.infrastructure.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    Page<Report> findByReportedTypeAndReportedIdOrderByCreatedAtDesc(ReportType type, Long reportedId, Pageable pageable);

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByProcessedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Report> findTop5ByReportedTypeAndReportedIdOrderByCreatedAtDesc(ReportType type, Long reportedId);

    // 사용자의 신고 내역 페이징 조회
    Page<Report> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 사용자와 상태별 신고 카운트
    long countByUserAndStatus(User user, ReportStatus status);
    
    // 사용자의 첫 신고일 조회
    @Query("SELECT MIN(r.createdAt) FROM Report r WHERE r.user = :user")
    LocalDateTime findFirstReportDateByUser(@Param("user") User user);
    
    // 사용자의 마지막 신고일 조회
    @Query("SELECT MAX(r.createdAt) FROM Report r WHERE r.user = :user")
    LocalDateTime findLastReportDateByUser(@Param("user") User user);
    
    // 모든 신고자 조회 (중복 제거)
    @Query("SELECT DISTINCT r.user FROM Report r")
    List<User> findDistinctReporters();
    
    // 콘텐츠 소유자별 신고 받은 횟수 조회
    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportedType = 'ARTWORK' AND r.reportedId IN (SELECT a.id FROM Artwork a WHERE a.user.id = :ownerId) OR r.reportedType = 'CLASS' AND r.reportedId IN (SELECT w.id FROM WorkShop w WHERE w.owner.id = :ownerId) OR r.reportedType = 'REVIEW' AND r.reportedId IN (SELECT rv.id FROM Review rv WHERE rv.user.id = :ownerId)")
    long countReportsByContentOwner(@Param("ownerId") Long ownerId);
    
    // 신고 타입별 카운트
    long countByReportedType(ReportType type);
    
    // 신고 타입과 상태별 카운트
    long countByReportedTypeAndStatus(ReportType type, ReportStatus status);
    
    // 가장 많이 신고된 콘텐츠 조회 (타입별 상위 N개)
    @Query("SELECT r.reportedId, COUNT(r) as count FROM Report r WHERE r.reportedType = :type GROUP BY r.reportedId ORDER BY count DESC")
    List<Object[]> findTopReportedContentByType(@Param("type") ReportType type, Pageable pageable);
    
    // 가장 많이 신고된 콘텐츠 조회 (편의 메서드)
    default List<Object[]> findTopReportedContentByType(ReportType type, int limit) {
        return findTopReportedContentByType(type, PageRequest.of(0, limit));
    }
    
    // 사용자와 상태별 신고 조회
    List<Report> findByUserAndStatus(User user, ReportStatus status);

}
