package com.webbeat.webbeat.repository;

import com.webbeat.webbeat.model.DailyStat;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailyStatRepository extends MongoRepository<DailyStat, String> {

    List<DailyStat> findByOwnerIdAndDateAfterOrderByDateAsc(String ownerId, LocalDate date);

}