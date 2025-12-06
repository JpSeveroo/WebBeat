package com.webbeat.webbeat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "daily_stats")
public record DailyStat(
        @Id
        String id,
        String ownerId,
        String monitoredId,
        LocalDate date,
        long totalChecks,
        long successChecks,
        double uptimePercentage
) {}