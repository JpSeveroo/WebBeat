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

    @Autowired
    private MongoTemplate mongoTemplate;

    public DashService(LogRepository logRepository, MonitoredRepository monitoredRepository, DailyStatRepository dailyStatRepository) {
        this.logRepository = logRepository;
        this.monitoredRepository = monitoredRepository;
        this.dailyStatRepository = dailyStatRepository;
    }

    /**
     * Calcula todas as métricas do Dashboard para o usuário logado (userId).
     */
    public DashboardStatsDTO getDashboardStats(String userId) {

        // --- 1. Métrica: Total de URLs (Filtro por Usuário) ---
        // Contagem de todos os serviços monitorados pertencentes ao usuário.
        long totalUrls = monitoredRepository.countByOwnerId(userId);

        // --- 2. Métrica: Total de Alertas (Filtro por Usuário) ---
        // Contagem de todos os logs do usuário onde o status code NÃO é 200 (OK).
        long totalAlerts = logRepository.countByOwnerIdAndStatusCodeNot(userId, 200);

        // --- 3. Métrica: Porcentagem de Uptime (Análise dos últimos 24h) ---
        // ATENÇÃO: Esta métrica é falha para cenários complexos (diferentes intervalos),
        // mas é mantida como métrica rápida de 24h até a agregação ser implementada.
        Instant last24Hours = Instant.now().minus(Duration.ofHours(24));
        List<LogEntry> recentLogs = logRepository.findByOwnerIdAndTimestampAfter(userId, last24Hours);   // Busca logs brutos recentes

        long totalChecks = recentLogs.size();
        long successChecks = recentLogs.stream().filter(log -> log.statusCode() == 200).count();

        double uptimePercentage = 0.0;
        if (totalChecks > 0) {
            uptimePercentage = (double) successChecks / totalChecks * 100.0;
        }

        // --- 4. Métrica: Serviços Online (Status Atual) ---
        // Busca TODAS as URLs para verificar o status atual de cada uma.
        List<Monitored> allMonitored = monitoredRepository.findByOwnerId(userId);
        long servicesOnline = allMonitored.stream()
                .filter(monitored -> {
                    Optional<LogEntry> latestLog = logRepository
                            .findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc(userId, monitored.id());
                    return latestLog.isPresent() && latestLog.get().statusCode() == 200;
                })
                .count();

        // --- 5. Métrica: Histórico de Uptime (USANDO DADOS REAIS AGREGADOS) ---
        // Chamamos o metodo auxiliar que consulta a coleção de Roll-ups (DailyStat).
        List<ChartDataPointDTO> uptimeHistory = getAggregatedUptimeHistory(userId);

        return new DashboardStatsDTO(
                totalUrls,
                servicesOnline,
                uptimePercentage,
                totalAlerts,
                uptimeHistory
        );
    }

    /**
     * Busca os dados agregados (Roll-ups) no DailyStatRepository para o Gráfico de Linha.
     * Esta consulta resolve o problema da Explosão de Logs para relatórios de longo prazo.
     */
    private List<ChartDataPointDTO> getAggregatedUptimeHistory(String userId) {

        // 1. Define o período (Últimos 7 dias)
        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);

        // 2. Busca os dados agregados (DailyStat) no NOVO REPOSITÓRIO
        List<DailyStat> stats = dailyStatRepository.findByOwnerIdAndDateAfterOrderByDateAsc(userId, oneWeekAgo);

        if (stats.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Converte o modelo DailyStat para o DTO de gráfico (ChartDataPointDTO)
        List<ChartDataPointDTO> history = new ArrayList<>();
        for (DailyStat stat : stats) {
            String label = stat.date().getDayOfWeek().toString().substring(0, 3);
            history.add(new ChartDataPointDTO(label, stat.uptimePercentage()));
        }

        return history;
    }
}