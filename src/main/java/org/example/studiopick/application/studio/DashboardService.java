package org.example.studiopick.application.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.DashboardResponseDto;
import org.example.studiopick.infrastructure.studio.mybatis.StudioDashboardStatsMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;


public interface DashboardService {

    DashboardResponseDto getStudioDashboard(Long studioId);
}
