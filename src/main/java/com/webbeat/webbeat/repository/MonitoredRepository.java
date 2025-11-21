package com.webbeat.webbeat.repository;

import com.webbeat.webbeat.model.Monitored;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MonitoredRepository extends MongoRepository<Monitored, String> {
    List<Monitored> repoFindByOwnerId(String ownerId);

    boolean existsByOwnerIdAndLink(String ownerId, String link);
}
