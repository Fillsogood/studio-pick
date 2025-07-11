package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.refund.RefundService;
import org.example.studiopick.application.refund.dto.DailyRefundStatsDto;
import org.example.studiopick.application.refund.dto.RefundHistoryResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.domain.refund.Refund;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ✅ 관리자용 환불 내역 관리 컨트롤러
 */
@Tag(name = "Admin Refund", description = "관리자 환불 내역 관리")
@RestController
@RequestMapping("/api/v1/admin/refunds")
@RequiredArgsConstructor
@Slf4j
public class AdminRefundController {

    private final RefundService refundService;

    /**
     * 환불 실패 건 조회 (관리자용)
     */
    @Operation(summary = "환불 실패 건 조회", description = "환불에 실패한 모든 건을 조회합니다.")
    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<RefundHistoryResponse>>> getFailedRefunds() {
        
        log.info("환불 실패 건 조회 요청");
        
        List<Refund> failedRefunds = refundService.getFailedRefunds();
        List<RefundHistoryResponse> responses = failedRefunds.stream()
            .map(RefundHistoryResponse::from)
            .toList();
            
        log.info("환불 실패 건 조회 완료: count={}", responses.size());

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, responses, "환불 실패 건을 성공적으로 조회했습니다."));
    }

    /**
     * 일별 환불 통계 조회 (관리자용)
     */
    @Operation(summary = "일별 환불 통계 조회", description = "지정된 기간의 일별 환불 통계를 조회합니다.")
    @GetMapping("/stats/daily")
    public ResponseEntity<ApiResponse<List<DailyRefundStatsDto>>> getDailyRefundStats(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        
        log.info("일별 환불 통계 조회 요청: startDate={}, endDate={}", startDate, endDate);
        
        List<DailyRefundStatsDto> response = refundService.getDailyRefundStats(startDate, endDate);
            
        log.info("일별 환불 통계 조회 완료: count={}", response.size());

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, response, "일별 환불 통계를 성공적으로 조회했습니다."));
    }

    /**
     * 전체 환불 내역 조회 (관리자용)
     */
    @Operation(summary = "전체 환불 내역 조회", description = "모든 환불 내역을 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RefundHistoryResponse>>> getAllRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("전체 환불 내역 조회 요청: page={}, size={}", page, size);
        
        // TODO: 페이징 처리 로직 추가 필요
        throw new UnsupportedOperationException("전체 환불 내역 조회 기능은 페이징 처리 후 구현 예정입니다.");
    }

    /**
     * 환불 재처리 (관리자용)
     */
    @Operation(summary = "환불 재처리", description = "실패한 환불을 재처리합니다.")
    @PostMapping("/{refundId}/retry")
    public ResponseEntity<ApiResponse<String>> retryRefund(@PathVariable Long refundId) {
        
        log.info("환불 재처리 요청: refundId={}", refundId);
        
        // TODO: 환불 재처리 로직 구현 필요
        throw new UnsupportedOperationException("환불 재처리 기능은 아직 구현되지 않았습니다.");
    }
}
