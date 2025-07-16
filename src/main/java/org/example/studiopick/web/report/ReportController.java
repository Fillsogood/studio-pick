package org.example.studiopick.web.report;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.report.ReportService;
import org.example.studiopick.application.report.dto.ReportRequestDto;
import org.example.studiopick.application.report.dto.ReportResponseDto;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ReportRequestDto request
    ) {
        // 토큰에서 직접 사용자 ID 추출
        Long userId = userPrincipal.getUserId();
        ReportResponseDto response = reportService.createReport(userId, request);
        return ResponseEntity.ok(response);
    }
}
