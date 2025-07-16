package org.example.studiopick.application.studio;

import org.example.studiopick.application.studio.dto.DashboardResponseDto;



public interface DashboardService {

    DashboardResponseDto getStudioDashboard(Long studioId);
}
