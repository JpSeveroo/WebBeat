package com.webbeat.webbeat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;

@Document(collection = "logs")
public record LogEntry(
        @Id
        String id,
        String ownerId,
        String monitoredId,
        @Indexed(expireAfterSeconds = 604800) // Índice TTL de 7 dias (7*24*60*60)
        Instant timestamp,
        Integer statusCode,
        Long responseTime
) {}

