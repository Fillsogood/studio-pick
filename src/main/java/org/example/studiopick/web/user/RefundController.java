package org.example.studiopick.web.user;

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

import java.util.List;

/**
 * ✅ 환불 내역 조회 컨트롤러
 */
@Tag(name = "Refund", description = "환불 내역 관리")
@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
@Slf4j
public class RefundController {

    private final RefundService refundService;

    /**
     * 사용자별 환불 내역 조회
     */
    @Operation(summary = "사용자 환불 내역 조회", description = "로그인한 사용자의 모든 환불 내역을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RefundHistoryResponse>>> getUserRefundHistory(
            @PathVariable Long userId) {
        
        log.info("사용자 환불 내역 조회 요청: userId={}", userId);
        
        List<Refund> refunds = refundService.getUserRefundHistory(userId);
        List<RefundHistoryResponse> responses = refunds.stream()
            .map(RefundHistoryResponse::from)
            .toList();
            
        log.info("사용자 환불 내역 조회 완료: userId={}, count={}", userId, responses.size());

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, responses, "환불 내역을 성공적으로 조회했습니다."));
    }

    /**
     * 예약별 환불 내역 조회
     */
    @Operation(summary = "예약별 환불 내역 조회", description = "특정 예약의 환불 내역을 조회합니다.")
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<List<RefundHistoryResponse>>> getReservationRefundHistory(
            @PathVariable Long reservationId) {
        
        log.info("예약 환불 내역 조회 요청: reservationId={}", reservationId);
        
        List<Refund> refunds = refundService.getReservationRefundHistory(reservationId);
        List<RefundHistoryResponse> responses = refunds.stream()
            .map(RefundHistoryResponse::from)
            .toList();
            
        log.info("예약 환불 내역 조회 완료: reservationId={}, count={}", reservationId, responses.size());

        return ResponseEntity.ok(new org.example.studiopick.common.dto.ApiResponse<>(
            true, responses, "예약 환불 내역을 성공적으로 조회했습니다."));
    }

    /**
     * 환불 내역 상세 조회
     */
    @Operation(summary = "환불 내역 상세 조회", description = "특정 환불 내역의 상세 정보를 조회합니다.")
    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<RefundHistoryResponse>> getRefundDetail(
            @PathVariable Long refundId) {
        
        log.info("환불 상세 조회 요청: refundId={}", refundId);
        
        // 환불 내역 조회 로직 (RefundService에 메서드 추가 필요)
        // 임시로 예외 처리
        throw new UnsupportedOperationException("환불 상세 조회 기능은 아직 구현되지 않았습니다.");
    }
}
