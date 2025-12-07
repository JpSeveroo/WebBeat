package com.webbeat.webbeat.service;

import com.webbeat.webbeat.dto.DashboardStatsDTO;
import com.webbeat.webbeat.dto.ChartDataPointDTO;
import com.webbeat.webbeat.model.LogEntry;
import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.model.DailyStat; // NOVO IMPORT: Modelo DailyStat
import com.webbeat.webbeat.repository.LogRepository;
import com.webbeat.webbeat.repository.MonitoredRepository;
import com.webbeat.webbeat.repository.DailyStatRepository; // NOVO IMPORT: Repositório DailyStat
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.ArrayList;


@Service
public class DashService {

    private final LogRepository logRepository;
    private final MonitoredRepository monitoredRepository;
    private final DailyStatRepository dailyStatRepository;

    private final MongoTemplate mongoTemplate;

    public DashService(LogRepository logRepository, MonitoredRepository monitoredRepository, DailyStatRepository dailyStatRepository, MongoTemplate mongoTemplate) {
        this.logRepository = logRepository;
        this.monitoredRepository = monitoredRepository;
        this.dailyStatRepository = dailyStatRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public DashboardStatsDTO getDashboardStats(String userId) {

        long totalUrls = monitoredRepository.countByOwnerId(userId);

        long totalAlerts = logRepository.countByOwnerIdAndStatusCodeNot(userId, 200);

        Instant last24Hours = Instant.now().minus(Duration.ofHours(24));
        List<LogEntry> recentLogs = logRepository.findByOwnerIdAndTimestampAfter(userId, last24Hours);

        long totalChecks = recentLogs.size();
        long successChecks = recentLogs.stream().filter(log -> log.statusCode() == 200).count();

        double uptimePercentage = 0.0;
        if (totalChecks > 0) {
            uptimePercentage = (double) successChecks / totalChecks * 100.0;
        }

        List<Monitored> allMonitored = monitoredRepository.findByOwnerId(userId);
        long servicesOnline = allMonitored.stream()
                .filter(monitored -> {
                    Optional<LogEntry> latestLog = logRepository
                            .findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc(userId, monitored.id());
                    return latestLog.isPresent() && latestLog.get().statusCode() == 200;
                })
                .count();

        List<ChartDataPointDTO> uptimeHistory = getAggregatedUptimeHistory(userId);

        return new DashboardStatsDTO(
                totalUrls,
                servicesOnline,
                uptimePercentage,
                totalAlerts,
                uptimeHistory
        );
    }

    private List<ChartDataPointDTO> getAggregatedUptimeHistory(String userId) {

        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);

        List<DailyStat> stats = dailyStatRepository.findByOwnerIdAndDateAfterOrderByDateAsc(userId, oneWeekAgo);

        if (stats.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChartDataPointDTO> history = new ArrayList<>();
        for (DailyStat stat : stats) {
            String label = stat.date().getDayOfWeek().toString().substring(0, 3);
            history.add(new ChartDataPointDTO(label, stat.uptimePercentage()));
        }

        return history;
    }
}