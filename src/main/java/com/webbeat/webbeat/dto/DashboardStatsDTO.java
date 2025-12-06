package com.webbeat.webbeat.dto;

import java.util.List;

public record DashboardStatsDTO(
        long totalUrls,
        long servicesOnline,
        double uptimePercentage,
        long totalAlerts,

        List<ChartDataPointDTO> uptimeHistory
) {}
