package org.example.studiopick.application.admin.dto.dashboard;

import java.math.BigDecimal;

/**
 * 주요 지표 요약 응답
 */
public record AdminKpiSummaryResponse(
        
        // 운영 KPI
        OperationalKpi operationalKpi,
        
        // 품질 KPI
        QualityKpi qualityKpi,
        
        // 성장 KPI
        GrowthKpi growthKpi
) {
    public record OperationalKpi(
            double systemUptime,
            long averageResponseTime,
            double bookingSuccessRate,
            double paymentSuccessRate
    ) {}
    
    public record QualityKpi(
            double reportResolutionRate,
            long qualityComplaintCount,
            double contentModerationAccuracy
    ) {}
    
    public record GrowthKpi(
            double userGrowthRate,
            double studioGrowthRate,
            double revenueGrowthRate,
            double userRetentionRate,
            double studioRetentionRate
    ) {}
}
