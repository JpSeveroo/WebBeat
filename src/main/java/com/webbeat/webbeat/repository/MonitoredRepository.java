package com.webbeat.webbeat.repository;

import com.webbeat.webbeat.model.Monitored;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MonitoredRepository extends MongoRepository<Monitored, String> {
}
