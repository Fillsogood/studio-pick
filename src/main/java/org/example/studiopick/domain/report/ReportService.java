package org.example.studiopick.domain.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.report.dto.ReportRequestDto;
import org.example.studiopick.domain.report.dto.ReportResponseDto;
import org.example.studiopick.domain.report.service.AutoHideService;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AutoHideService autoHideService;
    
    // 자동 비공개 처리를 위한 신고 임계값 (application.yml에서 설정 가능)
    @Value("${app.report.auto-hide-threshold:3}")
    private int autoHideThreshold;

    @Transactional
    public ReportResponseDto createReport(Long userId, ReportRequestDto request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        boolean alreadyReported = reportRepository.existsByUserAndReportedTypeAndReportedId(
                user, request.reportedType(), request.reportedId());

        if (alreadyReported) {
            throw new IllegalStateException("이미 신고하셨습니다.");
        }

        Report report = Report.builder()
                .reportedType(request.reportedType())
                .reportedId(request.reportedId())
                .user(user)
                .reason(request.reason())
                .build();

        reportRepository.save(report);
        log.info("Report created - Type: {}, ID: {}, User: {}", 
                request.reportedType(), request.reportedId(), userId);

        // 자동 비공개 처리 로직
        long reportCount = reportRepository.countByReportedTypeAndReportedIdAndStatus(
                request.reportedType(), request.reportedId(), ReportStatus.PENDING
        );

        if (reportCount >= autoHideThreshold) {
            // 자동 비공개 처리
            boolean hideSuccess = autoHideService.autoHideContent(
                    request.reportedType(), request.reportedId());
            
            if (hideSuccess) {
                report.autoHide();
                reportRepository.save(report);
                log.warn("Content auto-hidden - Type: {}, ID: {}, Report Count: {}", 
                        request.reportedType(), request.reportedId(), reportCount);
                
                return new ReportResponseDto(report.getId(), 
                        "신고가 접수되었습니다. 신고 횟수가 " + autoHideThreshold + "회에 도달하여 해당 콘텐츠가 자동으로 비공개 처리되었습니다.");
            } else {
                log.error("Failed to auto-hide content - Type: {}, ID: {}", 
                         request.reportedType(), request.reportedId());
            }
        }

        return new ReportResponseDto(report.getId(), "신고가 접수되었습니다.");
    }
    
    /**
     * 관리자가 신고를 처리할 때 사용하는 메서드
     */
    @Transactional
    public void processReport(Long reportId, Long adminId, ReportStatus status, String adminComment) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));
        
        // 신고 상태에 따른 처리
        switch (status) {
            case REVIEWED:
                report.markAsReviewed(admin, adminComment);
                break;
            case RESTORED:
                report.restore(admin, adminComment);
                // 컨텐츠 복원
                autoHideService.restoreContent(report.getReportedType(), report.getReportedId());
                break;
            case DELETED:
                report.delete(admin, adminComment);
                // 컨텐츠 영구 삭제는 별도 로직 필요
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 신고 처리 상태입니다.");
        }
        
        reportRepository.save(report);
        log.info("Report processed - ID: {}, Status: {}, Admin: {}", reportId, status, adminId);
    }
    
    /**
     * 신고 상태별 카운트 조회
     */
    @Transactional(readOnly = true)
    public long getReportCount(ReportType reportType, Long reportedId, ReportStatus status) {
        return reportRepository.countByReportedTypeAndReportedIdAndStatus(reportType, reportedId, status);
    }
    
    /**
     * 특정 컨텐츠의 총 신고 횟수 조회
     */
    @Transactional(readOnly = true)
    public long getTotalReportCount(ReportType reportType, Long reportedId) {
        return reportRepository.countByReportedTypeAndReportedId(reportType, reportedId);
    }
}
