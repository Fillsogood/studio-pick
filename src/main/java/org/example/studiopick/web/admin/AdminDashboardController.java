package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminDashboardService;
import org.example.studiopick.application.admin.dto.dashboard.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "관리자 대시보드 API")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @Operation(summary = "메인 대시보드 조회", description = "관리자 메인 대시보드의 모든 통계 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboardData() {
        var response = adminDashboardService.getDashboardData();
        return ResponseEntity.ok(new ApiResponse<>(true, response, "대시보드 데이터를 조회했습니다."));
    }

    @GetMapping("/stats")
    @Operation(summary = "기간별 통계 조회", description = "지정된 기간의 상세 통계 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        var response = adminDashboardService.getDashboardStats(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "기간별 통계 데이터를 조회했습니다."));
    }

    @GetMapping("/realtime")
    @Operation(summary = "실시간 통계 조회", description = "현재 시점의 실시간 통계 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<AdminRealTimeStatsResponse>> getRealTimeStats() {
        var response = adminDashboardService.getRealTimeStats();
        return ResponseEntity.ok(new ApiResponse<>(true, response, "실시간 통계 데이터를 조회했습니다."));
    }

    @GetMapping("/kpi")
    @Operation(summary = "KPI 요약 조회", description = "주요 성과 지표(KPI) 요약 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<AdminKpiSummaryResponse>> getKpiSummary() {
        var response = adminDashboardService.getKpiSummary();
        return ResponseEntity.ok(new ApiResponse<>(true, response, "KPI 요약 데이터를 조회했습니다."));
    }

}
