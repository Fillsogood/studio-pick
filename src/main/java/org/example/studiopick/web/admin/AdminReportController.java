package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminReportService;
import org.example.studiopick.application.admin.dto.report.*;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Report", description = "관리자 신고 관리 API")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    @Operation(summary = "신고 목록 조회")
    public ResponseEntity<ApiResponse<Page<AdminReportListResponse>>> getReportList(AdminReportSearchCriteria criteria) {
        Page<AdminReportListResponse> response = adminReportService.getReportList(criteria);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "신고 목록을 조회했습니다."));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "신고 상세 조회")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> getReportDetail(@PathVariable Long reportId) {
        AdminReportDetailResponse response = adminReportService.getReportDetail(reportId);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "신고 상세 정보를 조회했습니다."));
    }

    @PostMapping("/{reportId}/process")
    @Operation(summary = "신고 처리")
    public ResponseEntity<ApiResponse<Void>> processReport(
        @PathVariable Long reportId,
        @RequestParam Long adminId,
        @Valid @RequestBody AdminReportProcessCommand command) {
        adminReportService.processReport(reportId, adminId, command);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "신고를 처리했습니다."));
    }

    @PostMapping("/process-batch")
    @Operation(summary = "여러 신고 일괄 처리")
    public ResponseEntity<ApiResponse<Void>> processBatchReports(
        @RequestParam List<Long> reportIds,
        @RequestParam Long adminId,
        @Valid @RequestBody AdminReportProcessCommand command) {
        adminReportService.processBatchReports(reportIds, adminId, command);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "여러 신고를 일괄 처리했습니다."));
    }

    @GetMapping("/stats")
    @Operation(summary = "신고 통계 조회")
    public ResponseEntity<ApiResponse<AdminReportStatsResponse>> getReportStats(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {
        AdminReportStatsResponse response = adminReportService.getReportStats(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "신고 통계를 조회했습니다."));
    }

    @GetMapping("/content")
    @Operation(summary = "특정 콘텐츠의 신고 목록 조회")
    public ResponseEntity<ApiResponse<List<AdminReportListResponse>>> getContentReports(
        @RequestParam ReportType type,
        @RequestParam Long contentId) {
        List<AdminReportListResponse> response = adminReportService.getContentReports(type, contentId);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "특정 콘텐츠의 신고 목록을 조회했습니다."));
    }

    @GetMapping("/auto-hidden")
    @Operation(summary = "자동 비공개 신고 목록 조회")
    public ResponseEntity<ApiResponse<List<AdminReportListResponse>>> getAutoHiddenReports() {
        List<AdminReportListResponse> response = adminReportService.getAutoHiddenReports();
        return ResponseEntity.ok(new ApiResponse<>(true, response, "자동 비공개 신고 목록을 조회했습니다."));
    }

    @GetMapping("/pending-count")
    @Operation(summary = "대기 중인 신고 수 조회")
    public ResponseEntity<ApiResponse<Long>> getPendingReportCount() {
        long count = adminReportService.getPendingReportCount();
        return ResponseEntity.ok(new ApiResponse<>(true, count, "대기 중인 신고 수를 조회했습니다."));
    }

    @GetMapping("/reason-stats")
    @Operation(summary = "신고 사유별 통계 조회")
    public ResponseEntity<ApiResponse<AdminReportReasonStatsResponse>> getReportReasonStats(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {
        AdminReportReasonStatsResponse response = adminReportService.getReportReasonStats(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "신고 사유별 통계를 조회했습니다."));
    }

    @GetMapping("/reporter/{userId}")
    @Operation(summary = "신고자 신고 이력 조회")
    public ResponseEntity<ApiResponse<AdminReporterHistoryResponse>> getReporterHistory(
        @PathVariable Long userId,
        @RequestParam int page,
        @RequestParam int size) {
        AdminReporterHistoryResponse response = adminReportService.getReporterHistory(userId, page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "신고자의 신고 이력을 조회했습니다."));
    }

    @GetMapping("/malicious")
    @Operation(summary = "악성 신고자 목록 조회")
    public ResponseEntity<ApiResponse<AdminMaliciousReporterListResponse>> getMaliciousReporters(
        @RequestParam int page,
        @RequestParam int size) {
        AdminMaliciousReporterListResponse response = adminReportService.getMaliciousReporters(page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "악성 신고자 목록을 조회했습니다."));
    }

    @PostMapping("/malicious/{userId}/block")
    @Operation(summary = "악성 신고자 차단")
    public ResponseEntity<ApiResponse<Void>> blockReporter(
        @PathVariable Long userId,
        @RequestParam String reason) {
        adminReportService.blockReporter(userId, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "악성 신고자를 차단했습니다."));
    }
}
