package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminWorkshopService;
import org.example.studiopick.application.admin.dto.workshop.WorkshopDTOs.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/workshops")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Workshop", description = "관리자 워크샵 관리 API")
public class AdminWorkshopController {

    private final AdminWorkshopService adminWorkshopService;

    @GetMapping
    @Operation(summary = "워크샵 목록 조회", description = "관리자용 워크샵 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<AdminWorkshopListResponse>> getWorkshops(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword) {

        var response = adminWorkshopService.getWorkshops(page, size, status, keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "워크샵 목록을 조회했습니다."));
    }

    @GetMapping("/{workshopId}")
    @Operation(summary = "워크샵 상세 조회", description = "특정 워크샵의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<AdminWorkshopDetailResponse>> getWorkshopDetail(@PathVariable Long workshopId) {
        var response = adminWorkshopService.getWorkshopDetail(workshopId);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "워크샵 상세 정보를 조회했습니다."));
    }

    @PostMapping("/{workshopId}/approval")
    @Operation(summary = "워크샵 승인/거부", description = "워크샵 등록을 승인하거나 거부합니다.")
    public ResponseEntity<ApiResponse<AdminWorkshopApprovalResponse>> approveWorkshop(
        @PathVariable Long workshopId,
        @Valid @RequestBody AdminWorkshopApprovalCommand command) {

        var response = adminWorkshopService.approveWorkshop(workshopId, command);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "워크샵 승인/거부가 처리되었습니다."));
    }

    @PatchMapping("/{workshopId}/status")
    @Operation(summary = "워크샵 상태 변경", description = "워크샵의 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<AdminWorkshopStatusResponse>> changeWorkshopStatus(
        @PathVariable Long workshopId,
        @Valid @RequestBody AdminWorkshopStatusCommand command) {

        var response = adminWorkshopService.changeWorkshopStatus(workshopId, command);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "워크샵 상태가 변경되었습니다."));
    }

    @DeleteMapping("/{workshopId}")
    @Operation(summary = "워크샵 삭제", description = "워크샵을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteWorkshop(@PathVariable Long workshopId, @RequestParam String reason) {
        adminWorkshopService.deleteWorkshop(workshopId, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "워크샵이 삭제되었습니다."));
    }

    @GetMapping("/stats")
    @Operation(summary = "워크샵 통계 조회", description = "전체 워크샵 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<AdminWorkshopStatsResponse>> getWorkshopStats() {
        var response = adminWorkshopService.getWorkshopStats();
        return ResponseEntity.ok(new ApiResponse<>(true, response, "워크샵 통계를 조회했습니다."));
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 워크샵 조회", description = "기간별 인기 워크샵을 조회합니다.")
    public ResponseEntity<ApiResponse<AdminPopularWorkshopResponse>> getPopularWorkshops(
        @RequestParam String period,
        @RequestParam int limit) {

        var response = adminWorkshopService.getPopularWorkshops(period, limit);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "인기 워크샵을 조회했습니다."));
    }

    @GetMapping("/reported")
    @Operation(summary = "신고된 워크샵 목록", description = "신고된 워크샵 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ReportedWorkshopDto>>> getReportedWorkshops(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {

        var response = adminWorkshopService.getReportedWorkshops(page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "신고된 워크샵 목록을 조회했습니다."));
    }

}
