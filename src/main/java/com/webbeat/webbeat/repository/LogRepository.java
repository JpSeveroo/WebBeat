package com.webbeat.webbeat.repository;

import com.webbeat.webbeat.model.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LogRepository extends MongoRepository<LogEntry, String> {

    List<LogEntry> findByOwnerIdAndTimestampAfter(String ownerId, Instant timestamp);
    long countByOwnerIdAndStatusCodeNot(String ownerId, Integer statusCode);

    Optional<LogEntry> findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc(String ownerId, String monitoredId);
}