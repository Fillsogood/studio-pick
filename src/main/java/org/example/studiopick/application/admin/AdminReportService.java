package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.report.*;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.report.ReportRepository;
import org.example.studiopick.application.report.ReportService;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService {
    
    private final ReportRepository reportRepository;
    private final ReportService reportService;
    private final JpaUserRepository userRepository;
    private final ArtworkRepository artworkRepository;
    private final JpaWorkShopRepository jpaWorkShopRepository;
    
    /**
     * 신고 목록 조회 (검색 조건 및 페이징 지원)
     */
//    @Transactional(readOnly = true)
//    public Page<AdminReportListResponse> getReportList(AdminReportSearchCriteria criteria) {
//        Pageable pageable = createPageable(criteria);
//
//        Page<Report> reports = findReportsByCriteria(criteria, pageable);
//
//        return reports.map(this::toListResponse);
//    }
    
    /**
     * 신고 상세 조회
     */
//    @Transactional(readOnly = true)
//    public AdminReportDetailResponse getReportDetail(Long reportId) {
//        Report report = reportRepository.findById(reportId)
//                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));
//
//        return toDetailResponse(report);
//    }
    
    /**
     * 신고 처리 (승인/거부/복원 등)
     */
    @Transactional
    public void processReport(Long reportId, Long adminId, AdminReportProcessCommand command) {
        reportService.processReport(reportId, adminId, command.status(), command.adminComment());
        log.info("Admin {} processed report {} with status {}", adminId, reportId, command.status());
    }
    
    /**
     * 여러 신고 일괄 처리
     */
    @Transactional
    public void processBatchReports(List<Long> reportIds, Long adminId, AdminReportProcessCommand command) {
        for (Long reportId : reportIds) {
            try {
                processReport(reportId, adminId, command);
            } catch (Exception e) {
                log.error("Failed to process report {}: {}", reportId, e.getMessage());
            }
        }
    }
    
    /**
     * 신고 통계 조회
     */
    @Transactional(readOnly = true)
    public AdminReportStatsResponse getReportStats(LocalDate startDate, LocalDate endDate) {
        // 전체 통계
        AdminReportStatsResponse.TotalStats totalStats = getTotalStats();
        
        // 일별 통계 (실제로는 DB 쿼리로 구현)
        List<AdminReportStatsResponse.DailyStats> dailyStats = List.of();
        
        // 타입별 통계 (실제로는 DB 쿼리로 구현)
        List<AdminReportStatsResponse.TypeStats> typeStats = List.of();
        
        // 가장 많이 신고된 콘텐츠 (실제로는 DB 쿼리로 구현)
        List<AdminReportStatsResponse.TopReportedContent> topReported = List.of();
        
        return new AdminReportStatsResponse(totalStats, dailyStats, typeStats, topReported);
    }
    
    /**
     * 특정 콘텐츠의 모든 신고 조회
     */
//    @Transactional(readOnly = true)
//    public List<AdminReportListResponse> getContentReports(ReportType type, Long contentId) {
//        List<Report> reports = reportRepository.findByReportedTypeAndReportedIdOrderByCreatedAtDesc(type, contentId);
//        return reports.stream()
//                .map(this::toListResponse)
//                .collect(Collectors.toList());
//    }
    
    /**
     * 자동 비공개된 콘텐츠 목록 조회
     */
//    @Transactional(readOnly = true)
//    public List<AdminReportListResponse> getAutoHiddenReports() {
//        List<Report> reports = reportRepository.findAutoHiddenReports();
//        return reports.stream()
//                .map(this::toListResponse)
//                .collect(Collectors.toList());
//    }
    
    /**
     * 대기 중인 신고 수 조회
     */
    @Transactional(readOnly = true)
    public long getPendingReportCount() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }
    
    // === Private Methods ===
    
    private Pageable createPageable(AdminReportSearchCriteria criteria) {
        Sort.Direction direction = "asc".equals(criteria.sortDirection()) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, criteria.sortBy());
        return PageRequest.of(criteria.page(), criteria.size(), sort);
    }
    
    private Page<Report> findReportsByCriteria(AdminReportSearchCriteria criteria, Pageable pageable) {
        // 실제로는 QueryDSL이나 MyBatis로 구현하는 것이 좋습니다.
        // 여기서는 기본적인 JPA 메서드만 사용
        
        if (criteria.status() != null) {
            return reportRepository.findByStatusOrderByCreatedAtDesc(criteria.status(), pageable);
        } else if (criteria.reportedType() != null) {
            return reportRepository.findByReportedTypeOrderByCreatedAtDesc(criteria.reportedType(), pageable);
        } else {
            return reportRepository.findAll(pageable);
        }
    }
    
//    private AdminReportListResponse toListResponse(Report report) {
//        String reportedContent = getContentTitle(report.getReportedType(), report.getReportedId());
//        String reporterName = report.getUser().getDisplayName();
//        String adminName = report.getAdmin() != null ? report.getAdmin().getDisplayName() : null;
//
//        return new AdminReportListResponse(
//                report.getId(),
//                report.getReportedType(),
//                report.getReportedId(),
//                reportedContent,
//                report.getUser().getId(),
//                reporterName,
//                report.getReason(),
//                report.getStatus(),
//                report.getCreatedAt(),
//                report.getProcessedAt(),
//                report.getAdmin() != null ? report.getAdmin().getId() : null,
//                adminName
//        );
//    }
    
//    private AdminReportDetailResponse toDetailResponse(Report report) {
//        // 신고자 정보
//        User reporter = report.getUser();
//        long reporterReportCount = reportRepository.countByUser(reporter);
//        AdminReportDetailResponse.ReporterInfo reporterInfo =
//                new AdminReportDetailResponse.ReporterInfo(
//                        reporter.getId(),
//                        reporter.getDisplayName(),
//                        reporter.getEmail(),
//                        reporterReportCount
//                );
//
//        // 콘텐츠 소유자 정보
//        User contentOwner = getContentOwner(report.getReportedType(), report.getReportedId());
//        AdminReportDetailResponse.ContentOwnerInfo contentOwnerInfo =
//                new AdminReportDetailResponse.ContentOwnerInfo(
//                        contentOwner.getId(),
//                        contentOwner.getDisplayName(),
//                        contentOwner.getEmail(),
//                        0L  // 임시값 - 실제로는 복잡한 쿼리 필요
//                );
//
//        // 관리자 처리 정보
//        AdminReportDetailResponse.AdminProcessInfo adminProcessInfo = null;
//        if (report.getAdmin() != null) {
//            adminProcessInfo = new AdminReportDetailResponse.AdminProcessInfo(
//                    report.getAdmin().getId(),
//                    report.getAdmin().getDisplayName(),
//                    report.getAdminComment(),
//                    report.getProcessedAt()
//            );
//        }
//
//        // 신고 통계
//        long totalReportCount = reportRepository.countByReportedTypeAndReportedId(
//                report.getReportedType(), report.getReportedId());
//        long pendingReportCount = reportRepository.countByReportedTypeAndReportedIdAndStatus(
//                report.getReportedType(), report.getReportedId(), ReportStatus.PENDING);
//
//        String contentTitle = getContentTitle(report.getReportedType(), report.getReportedId());
//        String imageUrl = getContentImageUrl(report.getReportedType(), report.getReportedId());
//
//        return new AdminReportDetailResponse(
//                report.getId(),
//                report.getReportedType(),
//                report.getReportedId(),
//                contentTitle,
//                imageUrl,
//                reporterInfo,
//                contentOwnerInfo,
//                report.getReason(),
//                report.getStatus(),
//                report.getCreatedAt(),
//                adminProcessInfo,
//                totalReportCount,
//                pendingReportCount
//        );
//    }

//    private String getContentTitle(ReportType type, Long contentId) {
//        switch (type) {
//            case ARTWORK:
//                return artworkRepository.findById(contentId)
//                        .map(Artwork::getTitle)
//                        .orElse("삭제된 작품");
//            case CLASS:
//                return jpaWorkShopRepository.findById(contentId)
//                        .map(WorkShop::getTitle)
//                        .orElse("삭제된 클래스");
//            case REVIEW:
//                return feedRepository.findById(contentId)
//                        .map(review -> "리뷰: " + (review.getComment().length() > 20 ?
//                                review.getComment().substring(0, 20) + "..." :
//                                review.getComment()))
//                        .orElse("삭제된 리뷰");
//            default:
//                return "알 수 없음";
//        }
//    }
    
    private String getContentImageUrl(ReportType type, Long contentId) {
        switch (type) {
            case ARTWORK:
                return artworkRepository.findById(contentId)
                        .map(Artwork::getImageUrl)
                        .orElse(null);
            case CLASS:
                return "/images/default-class.png";
            case REVIEW:
                return null;
            default:
                return null;
        }
    }
    
//    private User getContentOwner(ReportType type, Long contentId) {
//        switch (type) {
//            case ARTWORK:
//                return artworkRepository.findById(contentId)
//                        .map(Artwork::getUser)
//                        .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다."));
//            case CLASS:
//                return jpaWorkShopRepository.findById(contentId)
//                        .map(classEntity -> classEntity.getStudio().getOwner())
//                        .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));
//            case REVIEW:
//                return feedRepository.findById(contentId)
//                        .map(Feed::getUser)
//                        .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
//            default:
//                throw new IllegalArgumentException("지원하지 않는 신고 타입입니다.");
//        }
//    }
    
    private AdminReportStatsResponse.TotalStats getTotalStats() {
        long total = reportRepository.count();
        long pending = reportRepository.countByStatus(ReportStatus.PENDING);
        long processed = reportRepository.countByStatusNot(ReportStatus.PENDING);
        long autoHidden = reportRepository.countByStatus(ReportStatus.AUTO_HIDDEN);
        
        return new AdminReportStatsResponse.TotalStats(total, pending, processed, autoHidden);
    }
}