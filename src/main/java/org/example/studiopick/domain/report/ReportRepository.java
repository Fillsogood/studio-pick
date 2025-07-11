package org.example.studiopick.domain.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

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
    
    // 관리자용: 처리 대기 중인 신고 목록 조회
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);
    
    // 관리자용: 사용자별 신고 목록 조회
    List<Report> findByUserOrderByCreatedAtDesc(User user);
    
    // 관리자용: 신고 타입별 목록 조회
    List<Report> findByReportedTypeOrderByCreatedAtDesc(ReportType reportedType);
}
