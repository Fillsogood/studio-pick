package org.example.studiopick.web.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.DashboardResponseDto;
import org.example.studiopick.application.studio.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studio/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    // 대시보드 통계 조회 API
    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboard(@RequestParam Long studioId) {
        DashboardResponseDto dashboard = dashboardService.getStudioDashboard(studioId);
        return ResponseEntity.ok(dashboard);
    }
}
