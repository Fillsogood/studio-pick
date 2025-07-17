package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.report.*;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.common.enums.ArtworkStatus;
import org.example.studiopick.domain.common.enums.HideStatus;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.report.ReportRepository;
import org.example.studiopick.application.report.ReportService;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.example.studiopick.infrastructure.review.ReviewRepository;
import org.example.studiopick.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportServiceImpl implements AdminReportService {
    
    private final ReportRepository reportRepository;
    private final ReportService reportService;
    private final ArtworkRepository artworkRepository;
    private final JpaWorkShopRepository jpaWorkShopRepository;
    private final ReviewRepository reviewRepository;
    private final JpaUserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportListResponse> getReportList(AdminReportSearchCriteria criteria) {
        Pageable pageable = createPageable(criteria);
        Page<Report> reports = findReportsByCriteria(criteria, pageable);
        return reports.map(this::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReportDetailResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));
        return toDetailResponse(report);
    }


    @Override
    @Transactional(readOnly = true)
    public AdminReportReasonStatsResponse getReportReasonStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        // 실제 구현에서는 QueryDSL 또는 네이티브 쿼리를 사용하여 그룹별 집계 수행
        List<Report> reports = reportRepository.findByCreatedAtBetween(start, end);

        Map<String, Long> reasonCounts = reports.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Report::getReason,
                    java.util.stream.Collectors.counting()
                ));

        List<Report> resolvedReports = reportRepository.findByStatus(ReportStatus.RESTORED);

        Map<String, Long> resolvedReasonCounts = resolvedReports.stream()
            .collect(Collectors.groupingBy(
                Report::getReason,
                Collectors.counting()
            ));


        List<AdminReportReasonStatsResponse.ReasonStats> reasonStats = reasonCounts.entrySet().stream()
            .map(entry -> {
                long count = entry.getValue();
                long resolvedCount = resolvedReasonCounts.getOrDefault(entry.getKey(), 0L);
                double percentage = reports.size() > 0 ? (count * 100.0) / reports.size() : 0.0;
                double resolutionRate = count > 0 ? (resolvedCount * 100.0) / count : 0.0;

                return new AdminReportReasonStatsResponse.ReasonStats(
                    entry.getKey(),
                    count,
                    percentage,
                    resolvedCount,
                    resolutionRate
                );
            })
            .sorted((a, b) -> Long.compare(b.count(), a.count()))
            .toList();
        
        String mostCommonReason = reasonStats.isEmpty() ? "" : reasonStats.get(0).reason();
        double avgReportsPerReason = reasonCounts.isEmpty() ? 0.0 : 
                (double) reports.size() / reasonCounts.size();
        
        return new AdminReportReasonStatsResponse(
            startDate.toString(),
            endDate.toString(),
            reasonStats,
            new AdminReportReasonStatsResponse.TotalStats(
                (long) reports.size(), 
                (long) reasonCounts.size(), 
                mostCommonReason, 
                avgReportsPerReason
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReporterHistoryResponse getReporterHistory(Long userId, int page, int size) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 해당 사용자의 신고 내역 조회
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> userReports = reportRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        // 신고자 통계 계산
        long totalReports = reportRepository.countByUser(user);
        long approvedReports = reportRepository.countByUserAndStatus(user, ReportStatus.REVIEWED);
        long rejectedReports = reportRepository.countByUserAndStatus(user, ReportStatus.RESTORED);
        double approvalRate = totalReports > 0 ? (approvedReports * 100.0) / totalReports : 0.0;
        
        // 첫 신고일과 마지막 신고일
        LocalDateTime firstReportDate = reportRepository.findFirstReportDateByUser(user);
        LocalDateTime lastReportDate = reportRepository.findLastReportDateByUser(user);
        
        // 악성 신고자 여부 판단
        boolean isMalicious = approvalRate < 30.0 && totalReports >= 10; // 승인율 30% 미만, 신고 10건 이상
        
        AdminReporterHistoryResponse.ReporterStats stats = new AdminReporterHistoryResponse.ReporterStats(
            totalReports,
            approvedReports,
            rejectedReports,
            approvalRate,
            firstReportDate,
            lastReportDate,
            isMalicious
        );
        
        // 신고 내역 변환
        List<AdminReporterHistoryResponse.ReportHistoryItem> reportHistories = userReports.getContent().stream()
            .map(report -> new AdminReporterHistoryResponse.ReportHistoryItem(
                report.getId(),
                report.getReportedType().name(),
                report.getReportedId(),
                getContentTitle(report.getReportedType(), report.getReportedId()),
                report.getReason(),
                report.getStatus().name(),
                report.getCreatedAt(),
                report.getProcessedAt()
            ))
            .toList();
        
        return new AdminReporterHistoryResponse(
            userId,
            user.getName(),
            user.getEmail(),
            stats,
            reportHistories,
            new AdminReporterHistoryResponse.AdminPaginationInfo(
                page,
                userReports.getTotalElements(),
                userReports.getTotalPages()
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminMaliciousReporterListResponse getMaliciousReporters(int page, int size) {
        // 신고자별 통계 조회 (실제로는 QueryDSL이나 네이티브 쿼리 사용 권장)
        List<User> allReporters = reportRepository.findDistinctReporters();
        
        // 악성 신고자 필터링 및 정렬
        List<AdminMaliciousReporterListResponse.MaliciousReporter> maliciousReporters = allReporters.stream()
            .map(user -> {
                long totalReports = reportRepository.countByUser(user);
                long approvedReports = reportRepository.countByUserAndStatus(user, ReportStatus.REVIEWED);
                long rejectedReports = reportRepository.countByUserAndStatus(user, ReportStatus.RESTORED);
                double approvalRate = totalReports > 0 ? (approvedReports * 100.0) / totalReports : 0.0;
                
                LocalDateTime lastReportDate = reportRepository.findLastReportDateByUser(user);
                
                return new AdminMaliciousReporterListResponse.MaliciousReporter(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    totalReports,
                    approvedReports,
                    rejectedReports,
                    approvalRate,
                    lastReportDate,
                    calculateRiskLevel(approvalRate, totalReports),
                    user.getStatus() == UserStatus.LOCKED
                );
            })
            .filter(this::isReporterMalicious) // 악성 신고자만 필터링
            .sorted((a, b) -> Double.compare(a.approvalRate(), b.approvalRate())) // 승인율 낮은 순
            .toList();
        
        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, maliciousReporters.size());
        List<AdminMaliciousReporterListResponse.MaliciousReporter> pagedReporters = 
            maliciousReporters.subList(start, end);
        
        // 탐지 기준 (설정값으로 분리 가능)
        AdminMaliciousReporterListResponse.DetectionCriteria criteria = 
            new AdminMaliciousReporterListResponse.DetectionCriteria(
                30.0,  // 최대 승인율 30%
                10,    // 최소 신고 건수 10건
                5,     // 연속 거부 5건
                20.0   // 위험 레벨 기준 20%
            );
        
        return new AdminMaliciousReporterListResponse(
            pagedReporters,
            new AdminMaliciousReporterListResponse.AdminPaginationInfo(
                page,
                (long) maliciousReporters.size(),
                (int) Math.ceil((double) maliciousReporters.size() / size)
            ),
            criteria
        );
    }

    @Override
    @Transactional
    public void blockReporter(Long userId, String reason) {
        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 이미 차단된 사용자인지 확인
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new IllegalStateException("이미 차단된 사용자입니다.");
        }
        
        // 사용자 상태를 LOCKED로 변경
        user.changeStatus(UserStatus.LOCKED);
        
        // 사용자 정보 저장
        userRepository.save(user);
        
        // 해당 사용자의 대기 중인 신고들을 모두 거부 처리
        List<Report> pendingReports = reportRepository.findByUserAndStatus(user, ReportStatus.PENDING);
        for (Report report : pendingReports) {
            report.restore(null, "신고자 차단으로 인한 자동 거부");
        }
        
        // 차단 로그 기록
        log.info("Reporter {} blocked by admin. Reason: {}. Pending reports count: {}", 
                userId, reason, pendingReports.size());
    }
    
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
        
        // 일별 통계 조회
        List<AdminReportStatsResponse.DailyStats> dailyStats = getDailyStats(startDate, endDate);
        
        // 타입별 통계 조회
        List<AdminReportStatsResponse.TypeStats> typeStats = getTypeStats();
        
        // 가장 많이 신고된 콘텐츠 조회
        List<AdminReportStatsResponse.TopReportedContent> topReported = getTopReportedContent();
        
        return new AdminReportStatsResponse(totalStats, dailyStats, typeStats, topReported);
    }
    
    /**
     * 특정 콘텐츠의 모든 신고 조회
     */
    @Transactional(readOnly = true)
    public List<AdminReportListResponse> getContentReports(ReportType type, Long contentId) {
        List<Report> reports = reportRepository.findByReportedTypeAndReportedIdOrderByCreatedAtDesc(type, contentId);
        return reports.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 자동 비공개된 콘텐츠 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AdminReportListResponse> getAutoHiddenReports() {
        List<Report> reports = reportRepository.findAutoHiddenReports();
        return reports.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }
    
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
    
    private AdminReportListResponse toListResponse(Report report) {
        String reportedContent = getContentTitle(report.getReportedType(), report.getReportedId());
        String reporterName = report.getUser().getName();
        String adminName = report.getAdmin() != null ? report.getAdmin().getName() : null;

        return new AdminReportListResponse(
                report.getId(),
                report.getReportedType(),
                report.getReportedId(),
                reportedContent,
                report.getUser().getId(),
                reporterName,
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getProcessedAt(),
                report.getAdmin() != null ? report.getAdmin().getId() : null,
                adminName
        );
    }
    
    private AdminReportDetailResponse toDetailResponse(Report report) {
        // 신고자 정보
        AdminReportDetailResponse.ReporterInfo reporterInfo =
                new AdminReportDetailResponse.ReporterInfo(
                        report.getUser().getId(),
                        report.getUser().getName(),
                        report.getUser().getEmail(),
                        reportRepository.countByUser(report.getUser())
                );

        // 콘텐츠 소유자 정보 조회
        AdminReportDetailResponse.ContentOwnerInfo contentOwnerInfo = getContentOwnerInfo(
            report.getReportedType(), report.getReportedId());
        
        if (contentOwnerInfo == null) {
            contentOwnerInfo = new AdminReportDetailResponse.ContentOwnerInfo(
                null, "알 수 없음", "", 0L
            );
        }

        // 관리자 처리 정보
        AdminReportDetailResponse.AdminProcessInfo adminProcessInfo = null;
        if (report.getAdmin() != null) {
            adminProcessInfo = new AdminReportDetailResponse.AdminProcessInfo(
                    report.getAdmin().getId(),
                    report.getAdmin().getName(),
                    report.getAdminComment(),
                    report.getProcessedAt()
            );
        }

        // 신고 통계
        long totalReportCount = reportRepository.countByReportedTypeAndReportedId(
                report.getReportedType(), report.getReportedId());
        long pendingReportCount = reportRepository.countByReportedTypeAndReportedIdAndStatus(
                report.getReportedType(), report.getReportedId(), ReportStatus.PENDING);

        String contentTitle = getContentTitle(report.getReportedType(), report.getReportedId());
        String imageUrl = getContentImageUrl(report.getReportedType(), report.getReportedId());

        return new AdminReportDetailResponse(
                report.getId(),
                report.getReportedType(),
                report.getReportedId(),
                contentTitle,
                imageUrl,
                reporterInfo,
                contentOwnerInfo,
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt(),
                adminProcessInfo,
                totalReportCount,
                pendingReportCount
        );
    }
    
    private String getContentTitle(ReportType type, Long contentId) {
        switch (type) {
            case ARTWORK:
                return artworkRepository.findById(contentId)
                        .map(Artwork::getTitle)
                        .orElse("삭제된 작품");
            case CLASS:
                return jpaWorkShopRepository.findById(contentId)
                        .map(workshop -> workshop.getTitle())
                        .orElse("삭제된 클래스");
            case REVIEW:
                return reviewRepository.findById(contentId)
                        .map(review -> "리뷰: " + review.getComment().substring(0, Math.min(review.getComment().length(), 20)) + "...")
                        .orElse("삭제된 리뷰");
            default:
                return "알 수 없음";
        }
    }

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
    
    private AdminReportStatsResponse.TotalStats getTotalStats() {
        long total = reportRepository.count();
        long pending = reportRepository.countByStatus(ReportStatus.PENDING);
        long processed = reportRepository.countByStatusNot(ReportStatus.PENDING);
        long autoHidden = reportRepository.countByStatus(ReportStatus.AUTO_HIDDEN);
        
        return new AdminReportStatsResponse.TotalStats(total, pending, processed, autoHidden);
    }
    
    // ============ Private Helper Methods ============
    
    /**
     * 일별 통계 조회
     */
    private List<AdminReportStatsResponse.DailyStats> getDailyStats(LocalDate startDate, LocalDate endDate) {
        List<AdminReportStatsResponse.DailyStats> dailyStats = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(LocalTime.MAX);
            
            long dailyReports = reportRepository.countByCreatedAtBetween(dayStart, dayEnd);
            long dailyProcessed = reportRepository.countByProcessedAtBetween(dayStart, dayEnd);
            
            dailyStats.add(new AdminReportStatsResponse.DailyStats(
                current, dailyReports, dailyProcessed
            ));
            
            current = current.plusDays(1);
        }
        
        return dailyStats;
    }
    
    /**
     * 타입별 통계 조회
     */
    private List<AdminReportStatsResponse.TypeStats> getTypeStats() {
        List<AdminReportStatsResponse.TypeStats> typeStats = new ArrayList<>();
        
        for (ReportType type : ReportType.values()) {
            long count = reportRepository.countByReportedType(type);
            long pendingCount = reportRepository.countByReportedTypeAndStatus(type, ReportStatus.PENDING);
            
            typeStats.add(new AdminReportStatsResponse.TypeStats(
                type, count, pendingCount
            ));
        }
        
        return typeStats;
    }
    
    /**
     * 가장 많이 신고된 콘텐츠 조회
     */
    private List<AdminReportStatsResponse.TopReportedContent> getTopReportedContent() {
        // 실제로는 QueryDSL이나 네이티브 쿼리로 구현
        List<AdminReportStatsResponse.TopReportedContent> topReported = new ArrayList<>();
        
        // 예시: 작품별 신고 카운트 상위 5개
        List<Object[]> artworkCounts = reportRepository.findTopReportedContentByType(ReportType.ARTWORK, 5);
        for (Object[] result : artworkCounts) {
            Long contentId = (Long) result[0];
            Long count = (Long) result[1];
            String title = getContentTitle(ReportType.ARTWORK, contentId);
            boolean isHidden = checkIfContentIsHidden(ReportType.ARTWORK, contentId);

            topReported.add(new AdminReportStatsResponse.TopReportedContent(
                ReportType.ARTWORK, contentId, title, count, isHidden
            ));
        }
        
        return topReported;
    }
    
    /**
     * 콘텐츠가 숨겼지될는드 체크
     */
    private boolean checkIfContentIsHidden(ReportType type, Long contentId) {
        switch (type) {
            case ARTWORK:
                return artworkRepository.findById(contentId)
                        .map(artwork -> artwork.getHideStatus() != null && artwork.getHideStatus() != ArtworkStatus.PUBLIC)
                        .orElse(true); // 삭제된 경우 hidden으로 처리
            case CLASS:
                return jpaWorkShopRepository.findById(contentId)
                        .map(workshop -> workshop.getHideStatus() != null && workshop.getHideStatus() != HideStatus.OPEN)
                        .orElse(true);
            case REVIEW:
                return reviewRepository.findById(contentId)
                        .map(review -> !review.isVisible())
                        .orElse(true);
            default:
                return false;
        }
    }
    
    /**
     * 콘텐츠 소유자 정보 조회
     */
    private AdminReportDetailResponse.ContentOwnerInfo getContentOwnerInfo(ReportType type, Long contentId) {
        switch (type) {
            case ARTWORK:
                return artworkRepository.findById(contentId)
                    .map(artwork -> {
                        User owner = artwork.getUser();
                        long ownerReportCount = reportRepository.countReportsByContentOwner(owner.getId());
                        return new AdminReportDetailResponse.ContentOwnerInfo(
                            owner.getId(),
                            owner.getName(),
                            owner.getEmail(),
                            ownerReportCount
                        );
                    })
                    .orElse(null);
            case CLASS:
                return jpaWorkShopRepository.findById(contentId)
                    .map(workshop -> {
                        User owner = workshop.getOwner();
                        long ownerReportCount = reportRepository.countReportsByContentOwner(owner.getId());
                        return new AdminReportDetailResponse.ContentOwnerInfo(
                            owner.getId(),
                            owner.getName(),
                            owner.getEmail(),
                            ownerReportCount
                        );
                    })
                    .orElse(null);
            case REVIEW:
                return reviewRepository.findById(contentId)
                    .map(review -> {
                        User owner = review.getUser();
                        long ownerReportCount = reportRepository.countReportsByContentOwner(owner.getId());
                        return new AdminReportDetailResponse.ContentOwnerInfo(
                            owner.getId(),
                            owner.getName(),
                            owner.getEmail(),
                            ownerReportCount
                        );
                    })
                    .orElse(null);
            default:
                return null;
        }
    }
    
    /**
     * 신고자 위험 레벨 계산
     */
    private String calculateRiskLevel(double approvalRate, long totalReports) {
        if (approvalRate < 20.0 && totalReports >= 20) {
            return "HIGH";
        } else if (approvalRate < 40.0 && totalReports >= 10) {
            return "MEDIUM";
        } else if (approvalRate < 60.0 && totalReports >= 5) {
            return "LOW";
        } else {
            return "NORMAL";
        }
    }
    
    /**
     * 악성 신고자 여부 판단
     */
    private boolean isReporterMalicious(AdminMaliciousReporterListResponse.MaliciousReporter reporter) {
        return reporter.approvalRate() < 30.0 && reporter.totalReports() >= 10;
    }
}