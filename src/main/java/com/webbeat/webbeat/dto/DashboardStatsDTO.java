package com.webbeat.webbeat.dto;

public record DashboardStatsDTO(
        long totalUrls,
        long servicesOnline,
        double uptimePercentage,
        long totalAlerts
) {}
