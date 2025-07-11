package org.example.studiopick.application.admin.dto.report;

import org.example.studiopick.domain.common.enums.ReportType;

import java.time.LocalDate;
import java.util.List;

public record AdminReportStatsResponse(
        TotalStats totalStats,
        List<DailyStats> dailyStats,
        List<TypeStats> typeStats,
        List<TopReportedContent> topReportedContents
) {
    public record TotalStats(
            long totalReports,
            long pendingReports,
            long processedReports,
            long autoHiddenReports
    ) {}
    
    public record DailyStats(
            LocalDate date,
            long reportCount,
            long autoHiddenCount
    ) {}
    
    public record TypeStats(
            ReportType type,
            long count,
            long autoHiddenCount
    ) {}
    
    public record TopReportedContent(
            ReportType type,
            Long contentId,
            String contentTitle,
            long reportCount,
            boolean isHidden
    ) {}
}