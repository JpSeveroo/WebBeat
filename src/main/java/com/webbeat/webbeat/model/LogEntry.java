package com.webbeat.webbeat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "logs")
public record LogEntry(
        @Id
        String id,
        String ownerId,         // Chave Multi-Tenant: Quem é o dono do log
        String monitoredId,     // ID da URL checada
        Instant timestamp,
        Integer statusCode,     // Ex: 200, 404, 500
        Long responseTime       // Tempo em ms
) {}