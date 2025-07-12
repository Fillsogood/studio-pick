package org.example.studiopick.web.studio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.payment.SettlementService;
import org.example.studiopick.application.payment.dto.SettlementResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/settlements")
@Tag(name = "정산 API", description = "스튜디오 정산 내역 및 출금 요청 관련 API")
public class SettlementController {

    private final SettlementService settlementService;

    @Operation(summary = "정산 내역 조회", description = "스튜디오 ID를 기준으로 정산 내역을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<SettlementResponseDto>> getSettlements(
            @PathVariable Long studioId
    ) {
        List<SettlementResponseDto> result = settlementService.getSettlementsByStudio(studioId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "정산 출금 요청", description = "해당 정산 ID에 대해 출금 요청을 진행합니다. (상태: HOLD)")
    @PostMapping("/{settlementId}/withdraw")
    public ResponseEntity<String> requestWithdraw(
            @PathVariable Long settlementId
    ) {
        settlementService.withdrawSettlement(settlementId);
        return ResponseEntity.ok("출금 요청이 완료되었습니다.");
    }
}
