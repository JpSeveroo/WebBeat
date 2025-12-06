package com.webbeat.webbeat.dto;

public record ChartDataPointDTO(
        String label, // O rótulo (ex: "Mon", "10:00h", "20/Out")
        Double value  // O valor (ex: 99.5)
) {}