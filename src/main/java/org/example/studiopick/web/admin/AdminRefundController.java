package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.admin.AdminRefundService;
import org.example.studiopick.application.admin.dto.refund.RefundDTOs.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Refund", description = "관리자 환불 관리 API")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;

    @GetMapping
    @Operation(summary = "환불 요청 목록 조회")
    public ResponseEntity<ApiResponse<AdminRefundListResponse>> getRefunds(
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate) {
        AdminRefundListResponse response = adminRefundService.getRefunds(page, size, status, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "환불 요청 목록을 조회했습니다."));
    }

    @GetMapping("/{refundId}")
    @Operation(summary = "환불 상세 조회")
    public ResponseEntity<ApiResponse<AdminRefundDetailResponse>> getRefundDetail(@PathVariable Long refundId) {
        AdminRefundDetailResponse response = adminRefundService.getRefundDetail(refundId);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "환불 상세 정보를 조회했습니다."));
    }

    @PostMapping("/{refundId}/process")
    @Operation(summary = "환불 처리")
    public ResponseEntity<ApiResponse<AdminRefundProcessResponse>> processRefund(
        @PathVariable Long refundId,
        @Valid @RequestBody AdminRefundProcessCommand command) {
        AdminRefundProcessResponse response = adminRefundService.processRefund(refundId, command);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "환불 처리를 완료했습니다."));
    }

    @GetMapping("/stats")
    @Operation(summary = "환불 통계 조회")
    public ResponseEntity<ApiResponse<AdminRefundStatsResponse>> getRefundStats(
        @RequestParam String startDate,
        @RequestParam String endDate) {
        AdminRefundStatsResponse response = adminRefundService.getRefundStats(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "환불 통계를 조회했습니다."));
    }

    @GetMapping("/pending/count")
    @Operation(summary = "환불 대기 건수 조회")
    public ResponseEntity<ApiResponse<Long>> getPendingRefundCount() {
        long count = adminRefundService.getPendingRefundCount();
        return ResponseEntity.ok(new ApiResponse<>(true, count, "환불 대기 건수를 조회했습니다."));
    }

    @PostMapping("/bulk-process")
    @Operation(summary = "대량 환불 처리")
    public ResponseEntity<ApiResponse<AdminBulkRefundResponse>> processBulkRefunds(
        @Valid @RequestBody AdminBulkRefundCommand command) {
        AdminBulkRefundResponse response = adminRefundService.processBulkRefunds(command);
        return ResponseEntity.ok(new ApiResponse<>(true, response, "대량 환불 처리를 완료했습니다."));
    }

    @DeleteMapping("/{refundId}/cancel")
    @Operation(summary = "환불 취소")
    public ResponseEntity<ApiResponse<Void>> cancelRefund(
        @PathVariable Long refundId,
        @RequestParam String reason) {
        adminRefundService.cancelRefund(refundId, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "환불을 취소했습니다."));
    }
}
