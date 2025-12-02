package com.webbeat.webbeat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "logs")
public record LogEntry(
        @Id
        String id,
        String ownerId,
        String monitoredId,
        Instant timestamp,
        Integer statusCode,
        Long responseTime
) {}

