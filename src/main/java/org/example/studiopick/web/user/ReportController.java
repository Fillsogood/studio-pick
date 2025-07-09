package org.example.studiopick.web.user;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.report.ReportService;
import org.example.studiopick.domain.report.dto.ReportRequestDto;
import org.example.studiopick.domain.report.dto.ReportResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody ReportRequestDto request
    ) {
        ReportResponseDto response = reportService.createReport(userId, request);
        return ResponseEntity.ok(response);
    }
}
