package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.payment.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/settlements")
@Tag(name = "관리자 정산 API", description = "관리자 전용 정산 관리 기능")
public class AdminSettlementController {

    private final SettlementService settlementService;

    @PostMapping("/{settlementId}/approve")
    @Operation(summary = "정산 승인", description = "출금 요청(HOLD 상태)된 정산을 관리자 권한으로 승인(PAID) 처리합니다.")
    public ResponseEntity<String> approveSettlement(@PathVariable Long settlementId) {
        settlementService.approveSettlement(settlementId);
        return ResponseEntity.ok("정산이 승인되었습니다.");
    }
}
