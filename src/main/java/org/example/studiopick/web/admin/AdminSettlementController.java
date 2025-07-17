package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminSettlementService;
import org.example.studiopick.application.admin.dto.settlement.SettlementDTOs.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/settlements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Settlement", description = "관리자 정산 관리 API")
public class AdminSettlementController {

    private final AdminSettlementService adminSettlementService;

    @GetMapping
    @Operation(summary = "정산 대상 조회")
    public ResponseEntity<ApiResponse<AdminSettlementListResponse>> getSettlementTargets(
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate) {

        AdminSettlementListResponse response = adminSettlementService.getSettlementTargets(page, size, status, startDate, endDate);
        ApiResponse<AdminSettlementListResponse> apiResponse = new ApiResponse<>(true, response, "정산 대상 목록을 조회했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{settlementId}")
    @Operation(summary = "정산 상세 조회")
    public ResponseEntity<ApiResponse<AdminSettlementDetailResponse>> getSettlementDetail(@PathVariable Long settlementId) {
        AdminSettlementDetailResponse response = adminSettlementService.getSettlementDetail(settlementId);
        ApiResponse<AdminSettlementDetailResponse> apiResponse = new ApiResponse<>(true, response, "정산 상세 정보를 조회했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{settlementId}/process")
    @Operation(summary = "정산 처리")
    public ResponseEntity<ApiResponse<AdminSettlementProcessResponse>> processSettlement(
        @PathVariable Long settlementId,
        @Valid @RequestBody AdminSettlementProcessCommand command) {

        AdminSettlementProcessResponse response = adminSettlementService.processSettlement(settlementId, command);
        ApiResponse<AdminSettlementProcessResponse> apiResponse = new ApiResponse<>(true, response, "정산 처리를 완료했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/process-bulk")
    @Operation(summary = "대량 정산 처리")
    public ResponseEntity<ApiResponse<AdminBulkSettlementResponse>> processBulkSettlement(
        @Valid @RequestBody AdminBulkSettlementCommand command) {

        AdminBulkSettlementResponse response = adminSettlementService.processBulkSettlement(command);
        ApiResponse<AdminBulkSettlementResponse> apiResponse = new ApiResponse<>(true, response, "대량 정산 처리를 완료했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/stats")
    @Operation(summary = "정산 통계 조회")
    public ResponseEntity<ApiResponse<AdminSettlementStatsResponse>> getSettlementStats(
        @RequestParam String startDate,
        @RequestParam String endDate) {

        AdminSettlementStatsResponse response = adminSettlementService.getSettlementStats(startDate, endDate);
        ApiResponse<AdminSettlementStatsResponse> apiResponse = new ApiResponse<>(true, response, "정산 통계를 조회했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/studio/{studioId}")
    @Operation(summary = "스튜디오별 정산 내역")
    public ResponseEntity<ApiResponse<AdminStudioSettlementResponse>> getStudioSettlement(
        @PathVariable Long studioId,
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam(required = false) String status) {

        AdminStudioSettlementResponse response = adminSettlementService.getStudioSettlement(studioId, page, size, status);
        ApiResponse<AdminStudioSettlementResponse> apiResponse = new ApiResponse<>(true, response, "스튜디오별 정산 내역을 조회했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/workshop/{workshopId}")
    @Operation(summary = "워크샵별 정산 내역")
    public ResponseEntity<ApiResponse<AdminWorkshopSettlementResponse>> getWorkshopSettlement(
        @PathVariable Long workshopId,
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam(required = false) String status) {

        AdminWorkshopSettlementResponse response = adminSettlementService.getWorkshopSettlement(workshopId, page, size, status);
        ApiResponse<AdminWorkshopSettlementResponse> apiResponse = new ApiResponse<>(true, response, "워크샵별 정산 내역을 조회했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/commission")
    @Operation(summary = "수수료율 조회")
    public ResponseEntity<ApiResponse<AdminCommissionRateResponse>> getCommissionRates() {
        AdminCommissionRateResponse response = adminSettlementService.getCommissionRates();
        ApiResponse<AdminCommissionRateResponse> apiResponse = new ApiResponse<>(true, response, "수수료율을 조회했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/commission")
    @Operation(summary = "수수료율 업데이트")
    public ResponseEntity<ApiResponse<AdminCommissionRateResponse>> updateCommissionRate(
        @Valid @RequestBody AdminCommissionRateUpdateCommand command) {

        AdminCommissionRateResponse response = adminSettlementService.updateCommissionRate(command);
        ApiResponse<AdminCommissionRateResponse> apiResponse = new ApiResponse<>(true, response, "수수료율을 업데이트했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/report")
    @Operation(summary = "정산 보고서 생성")
    public ResponseEntity<ApiResponse<AdminSettlementReportResponse>> generateSettlementReport(
        @Valid @RequestBody AdminSettlementReportCommand command) {

        AdminSettlementReportResponse response = adminSettlementService.generateSettlementReport(command);
        ApiResponse<AdminSettlementReportResponse> apiResponse = new ApiResponse<>(true, response, "정산 보고서를 생성했습니다.");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{settlementId}/approval")
    @Operation(summary = "정산 승인/거부")
    public ResponseEntity<ApiResponse<AdminSettlementApprovalResponse>> approveSettlement(
        @PathVariable Long settlementId,
        @Valid @RequestBody AdminSettlementApprovalCommand command) {

        AdminSettlementApprovalResponse response = adminSettlementService.approveSettlement(settlementId, command);
        ApiResponse<AdminSettlementApprovalResponse> apiResponse = new ApiResponse<>(true, response, "정산 승인/거부 처리를 완료했습니다.");
        return ResponseEntity.ok(apiResponse);
    }
}
