package com.webbeat.webbeat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// É a URL
@Document(collection = "URLs")
public record Monitored(
        @Id
        String id,
        String ownerId,
        String name,
        String link,
        Integer port,
        String type,
        boolean beingMonitored,
        Instant monitoringStartTime
) {}
