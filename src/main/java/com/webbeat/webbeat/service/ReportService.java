package com.webbeat.webbeat.service;

import com.webbeat.webbeat.dto.DashboardStatsDTO;
import com.webbeat.webbeat.model.LogEntry;
import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.LogRepository;
import com.webbeat.webbeat.repository.MonitoredRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final LogRepository logRepository;
    private final MonitoredRepository monitoredRepository;

    public ReportService(LogRepository logRepository, MonitoredRepository monitoredRepository) {
        this.logRepository = logRepository;
        this.monitoredRepository = monitoredRepository;
    }

    /**
     * Calcula todas as métricas do Dashboard para o usuário logado (userId).
     * @param userId O ID do usuário logado (chave multi-tenant).
     * @return Um DTO com todas as estatísticas.
     */
    public DashboardStatsDTO getDashboardStats(String userId) {

        // --- 1. Métrica: Total de URLs (Filtro por Usuário) ---
        long totalUrls = monitoredRepository.countByOwnerId(userId);

        // --- 2. Métrica: Total de Alertas (Filtro por Usuário) ---
        // Conta todos os logs do usuário onde o status code NÃO é 200 (OK).
        long totalAlerts = logRepository.countByOwnerIdAndStatusCodeNot(userId, 200);


        // --- 3. Métrica: Porcentagem de Uptime (Análise dos últimos 24h) ---
        Instant last24Hours = Instant.now().minus(Duration.ofHours(24));

        // Busca todos os logs do usuário nas últimas 24 horas
        List<LogEntry> recentLogs = logRepository.findByOwnerIdAndTimestampAfter(userId, last24Hours);

        long totalChecks = recentLogs.size();
        long successChecks = recentLogs.stream()
                .filter(log -> log.statusCode() == 200)
                .count();

        double uptimePercentage = 0.0;
        if (totalChecks > 0) {
            uptimePercentage = (double) successChecks / totalChecks * 100.0;
        }


        // --- 4. Métrica: Serviços Online (Cálculo Otimizado) ---
        // 4.1. Busca TODAS as URLs do usuário.
        List<Monitored> allMonitored = monitoredRepository.findByOwnerId(userId);

        long servicesOnline = allMonitored.stream()
                // 4.2. Filtra a lista, mantendo apenas as URLs que estão ONLINE.
                .filter(monitored -> {

                    // 4.3. Para cada URL, busca o log mais recente (findTop...) no banco.
                    Optional<LogEntry> latestLog = logRepository
                            .findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc(userId, monitored.id());

                    // 4.4. A URL está online se o log existe E o status for 200.
                    return latestLog.isPresent() && latestLog.get().statusCode() == 200;
                })
                // 4.5. Conta quantos passaram pelo filtro.
                .count();

        // --- 5. Retorna o Objeto Final ---
        return new DashboardStatsDTO(
                totalUrls,
                servicesOnline,
                uptimePercentage,
                totalAlerts
        );
    }
}