package org.example.studiopick.application.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.domain.common.enums.*;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.application.report.dto.ReportRequestDto;
import org.example.studiopick.application.report.dto.ReportResponseDto;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.example.studiopick.infrastructure.report.ReportRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final JpaUserRepository userRepository;
    private final ArtworkRepository artworkRepository;
    private final JpaWorkShopRepository jpaWorkShopRepository;
    private final JpaStudioRepository jpaStudioRepository;

    @Value("${app.report.auto-hide-threshold:3}")
    private int autoHideThreshold;

    /**
     * 신고 생성 + 자동 비공개 처리
     */
    @Override
    @Transactional
    public ReportResponseDto createReport(Long userId, ReportRequestDto request) {
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
        log.info("Report created - Type: {}, ID: {}, User: {}", request.reportedType(), request.reportedId(), userId);

        long reportCount = reportRepository.countByReportedTypeAndReportedIdAndStatus(
            request.reportedType(), request.reportedId(), ReportStatus.PENDING
        );

        if (reportCount >= autoHideThreshold) {
            boolean hideSuccess = autoHideContent(request.reportedType(), request.reportedId());

            if (hideSuccess) {
                report.autoHide();
                reportRepository.save(report);
                log.warn("Content auto-hidden - Type: {}, ID: {}, Report Count: {}",
                    request.reportedType(), request.reportedId(), reportCount);

                return new ReportResponseDto(report.getId(),
                    "신고가 접수되었습니다. 신고 횟수가 " + autoHideThreshold + "회에 도달하여 해당 콘텐츠가 자동으로 비공개 처리되었습니다.");
            } else {
                log.error("Failed to auto-hide content - Type: {}, ID: {}", request.reportedType(), request.reportedId());
            }
        }

        return new ReportResponseDto(report.getId(), "신고가 접수되었습니다.");
    }

    /**
     * 신고 처리 (관리자)
     */
    @Override
    @Transactional
    public void processReport(Long reportId, Long adminId, ReportStatus status, String adminComment) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        switch (status) {
            case REVIEWED:
                report.markAsReviewed(admin, adminComment);
                break;
            case RESTORED:
                report.restore(admin, adminComment);
                restoreContent(report.getReportedType(), report.getReportedId());
                break;
            case DELETED:
                report.delete(admin, adminComment);
                // 컨텐츠 영구 삭제 로직이 필요하다면 별도 구현
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
    @Override
    @Transactional(readOnly = true)
    public long getReportCount(ReportType reportType, Long reportedId, ReportStatus status) {
        return reportRepository.countByReportedTypeAndReportedIdAndStatus(reportType, reportedId, status);
    }

    /**
     * 총 신고 카운트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public long getTotalReportCount(ReportType reportType, Long reportedId) {
        return reportRepository.countByReportedTypeAndReportedId(reportType, reportedId);
    }

    /**
     * 자동 비공개 처리
     */
    @Override
    @Transactional
    public boolean autoHideContent(ReportType reportType, Long reportedId) {
        try {
            switch (reportType) {
                case ARTWORK:
                    return hideArtwork(reportedId);
                case CLASS:
                    return hideWorkShop(reportedId);
                case REVIEW:
                    return hideStudio(reportedId);
                default:
                    log.warn("Unknown report type: {}", reportType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Failed to auto hide content. Type: {}, ID: {}", reportType, reportedId, e);
            return false;
        }
    }

    private boolean hideArtwork(Long artworkId) {
        return artworkRepository.findById(artworkId)
            .map(artwork -> {
                artwork.ArtworkChangeStatus(ArtworkStatus.REPORTED);
                artworkRepository.save(artwork);
                log.info("Artwork {} auto-hidden due to reports", artworkId);
                return true;
            }).orElse(false);
    }

    private boolean hideWorkShop(Long workShopId) {
        return jpaWorkShopRepository.findById(workShopId)
            .map(workShop -> {
                workShop.WorkShopChangeStatus(HideStatus.REPORTED);
                jpaWorkShopRepository.save(workShop);
                log.info("WorkShop {} auto-hidden due to reports", workShopId);
                return true;
            }).orElse(false);
    }

    private boolean hideStudio(Long studioId) {
        return jpaStudioRepository.findById(studioId)
            .map(studio  -> {
                studio.StudioChangeStatus(HideStatus.REPORTED);
                jpaStudioRepository.save(studio);
                log.info("Studio {} auto-hidden due to reports", studioId);
                return true;
            }).orElse(false);
    }

    /**
     * 비공개된 컨텐츠 복원
     */
    @Override
    @Transactional
    public void restoreContent(ReportType reportType, Long reportedId) {
        try {
            switch (reportType) {
                case ARTWORK -> restoreArtwork(reportedId);
                case CLASS -> restoreWorkShop(reportedId);
                case REVIEW -> restoreStudio(reportedId);
                default -> {
                    log.warn("Unknown report type: {}", reportType);
                }
            }
        } catch (Exception e) {
            log.error("Failed to restore content. Type: {}, ID: {}", reportType, reportedId, e);
        }
    }

    private boolean restoreArtwork(Long artworkId) {
        return artworkRepository.findById(artworkId)
            .map(artwork -> {
                if (artwork.getHideStatus() == ArtworkStatus.REPORTED) {
                    artwork.ArtworkChangeStatus(ArtworkStatus.PUBLIC);
                    artworkRepository.save(artwork);
                    log.info("Artwork {} restored", artworkId);
                    return true;
                }
                return false;
            }).orElse(false);
    }

    private boolean restoreWorkShop(Long classId) {
        return jpaWorkShopRepository.findById(classId)
            .map(workShop -> {
                if (workShop.getHideStatus() == HideStatus.REPORTED) {
                    workShop.WorkShopChangeStatus(HideStatus.OPEN);
                    jpaWorkShopRepository.save(workShop);
                    log.info("Class {} restored", classId);
                    return true;
                }
                return false;
            }).orElse(false);
    }

    private boolean restoreStudio(Long studioId) {
        return jpaStudioRepository.findById(studioId)
            .map(studio -> {
                if (studio.getHideStatus() == HideStatus.REPORTED) {
                    studio.StudioChangeStatus(HideStatus.OPEN);
                    jpaStudioRepository.save(studio);
                    log.info("Review {} restored", studioId);
                    return true;
                }
                return false;
            }).orElse(false);
    }
}
