package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.admin.AdminReportService;
import org.example.studiopick.application.admin.dto.report.*;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin Report", description = "관리자 신고 처리 API")
public class AdminReportController {
    
    private final AdminReportService adminReportService;
    
    @GetMapping
    @Operation(summary = "신고 목록 조회", description = "검색 조건에 따른 신고 목록을 페이징하여 조회합니다.")
    public ResponseEntity<Page<AdminReportListResponse>> getReportList(
            @Parameter(description = "신고 타입") @RequestParam(required = false) ReportType reportedType,
            @Parameter(description = "신고 상태") @RequestParam(required = false) ReportStatus status,
            @Parameter(description = "신고자 ID") @RequestParam(required = false) Long reporterId,
            @Parameter(description = "콘텐츠 소유자 ID") @RequestParam(required = false) Long contentOwnerId,
            @Parameter(description = "시작 일시") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "종료 일시") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        AdminReportSearchCriteria criteria = new AdminReportSearchCriteria(
                reportedType, status, reporterId, contentOwnerId, 
                startDate, endDate, keyword, page, size, sortBy, sortDirection
        );
        
        Page<AdminReportListResponse> reports = adminReportService.getReportList(criteria);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/{reportId}")
    @Operation(summary = "신고 상세 조회", description = "특정 신고의 상세 정보를 조회합니다.")
    public ResponseEntity<AdminReportDetailResponse> getReportDetail(
            @Parameter(description = "신고 ID") @PathVariable Long reportId
    ) {
        AdminReportDetailResponse report = adminReportService.getReportDetail(reportId);
        return ResponseEntity.ok(report);
    }
    
    @PutMapping("/{reportId}/process")
    @Operation(summary = "신고 처리", description = "특정 신고를 처리합니다.")
    public ResponseEntity<Void> processReport(
            @Parameter(description = "신고 ID") @PathVariable Long reportId,
            @Parameter(description = "관리자 ID") @RequestHeader("X-ADMIN-ID") Long adminId,
            @Valid @RequestBody AdminReportProcessCommand command
    ) {
        adminReportService.processReport(reportId, adminId, command);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/batch/process")
    @Operation(summary = "신고 일괄 처리", description = "여러 신고를 일괄로 처리합니다.")
    public ResponseEntity<Void> processBatchReports(
            @Parameter(description = "관리자 ID") @RequestHeader("X-ADMIN-ID") Long adminId,
            @Parameter(description = "신고 ID 목록") @RequestParam List<Long> reportIds,
            @Valid @RequestBody AdminReportProcessCommand command
    ) {
        adminReportService.processBatchReports(reportIds, adminId, command);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats")
    @Operation(summary = "신고 통계 조회", description = "지정된 기간의 신고 통계를 조회합니다.")
    public ResponseEntity<AdminReportStatsResponse> getReportStats(
            @Parameter(description = "시작 날짜") @RequestParam 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "종료 날짜") @RequestParam 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        AdminReportStatsResponse stats = adminReportService.getReportStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/content/{type}/{contentId}")
    @Operation(summary = "특정 콘텐츠 신고 조회", description = "특정 콘텐츠에 대한 모든 신고를 조회합니다.")
    public ResponseEntity<List<AdminReportListResponse>> getContentReports(
            @Parameter(description = "콘텐츠 타입") @PathVariable ReportType type,
            @Parameter(description = "콘텐츠 ID") @PathVariable Long contentId
    ) {
        List<AdminReportListResponse> reports = adminReportService.getContentReports(type, contentId);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/auto-hidden")
    @Operation(summary = "자동 비공개 신고 조회", description = "자동으로 비공개 처리된 신고 목록을 조회합니다.")
    public ResponseEntity<List<AdminReportListResponse>> getAutoHiddenReports() {
        List<AdminReportListResponse> reports = adminReportService.getAutoHiddenReports();
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/pending/count")
    @Operation(summary = "대기 중인 신고 수 조회", description = "처리 대기 중인 신고의 개수를 조회합니다.")
    public ResponseEntity<Long> getPendingReportCount() {
        long count = adminReportService.getPendingReportCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/urgent")
    @Operation(summary = "긴급 신고 조회", description = "긴급 처리가 필요한 신고를 조회합니다.")
    public ResponseEntity<List<AdminReportListResponse>> getUrgentReports() {
        // 특정 키워드나 신고 횟수가 많은 콘텐츠를 긴급으로 분류
        AdminReportSearchCriteria criteria = new AdminReportSearchCriteria(
                null, ReportStatus.PENDING, null, null, 
                null, null, null, 0, 10, "createdAt", "desc"
        );
        
        Page<AdminReportListResponse> reports = adminReportService.getReportList(criteria);
        return ResponseEntity.ok(reports.getContent());
    }
}