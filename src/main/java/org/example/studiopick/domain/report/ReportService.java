package org.example.studiopick.domain.report;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.report.dto.ReportRequestDto;
import org.example.studiopick.domain.report.dto.ReportResponseDto;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService{

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

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

        // 자동 비공개 처리 로직 (신고 3회 이상이면)
        long reportCount = reportRepository.countByReportedTypeAndReportedIdAndStatus(
                request.reportedType(), request.reportedId(), ReportStatus.PENDING
        );

        if (reportCount >= 3) {
            report.autoHide();
            // TODO: 실제로 Artwork, Class, Review 상태 비공개 처리도 함께 해야 함
        }

        return new ReportResponseDto(report.getId(), "신고가 접수되었습니다.");
    }

}
