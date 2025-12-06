package com.webbeat.webbeat.service;

import com.webbeat.webbeat.dto.MonStatusDTO;
import com.webbeat.webbeat.dto.MonitoredDTO;
import com.webbeat.webbeat.model.LogEntry;
import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.LogRepository;
import com.webbeat.webbeat.repository.MonitoredRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MonitoredService {

    private final MonitoredRepository monitoredRepository;
    private final LogRepository logRepository;

    public MonitoredService(MonitoredRepository monitoredRepository, LogRepository logRepository) {
        this.monitoredRepository = monitoredRepository;
        this.logRepository = logRepository;
    }

    public List<Monitored> monFindByOwnerId(String ownerId) {
        return monitoredRepository.findByOwnerId(ownerId);
    }

    public Monitored monFindByIdAndOwner(String Id, String ownerId) {
        return monitoredRepository.findById(Id)
                .filter(monitored -> monitored.ownerId().equals(ownerId))
                .orElseThrow(() -> new IllegalStateException("Monitored URL not found or access denied"));
    }

    public Monitored registerNewMonitored(MonitoredDTO monitoredDTO, String ownerId) {

        if (monitoredRepository.existsByOwnerIdAndLink(ownerId, monitoredDTO.link())) {
            throw new IllegalStateException("This link is already in your monitored list.");
        }

        Monitored newMonitored = new Monitored(
                null,
                ownerId,
                monitoredDTO.name(),
                monitoredDTO.link(),
                monitoredDTO.port(),
                monitoredDTO.type(),
                false,
                null,
                monitoredDTO.interval() != null ? monitoredDTO.interval() : 30
        );
        return monitoredRepository.save(newMonitored);
    }

    public Monitored updateStatusAndInterval(String id, String ownerId, boolean state, Integer newInterval) {
        Monitored existing = monFindByIdAndOwner(id, ownerId);
        Instant counter = state ? Instant.now() : null;

        Monitored updated = new Monitored(
                existing.id(),
                ownerId,
                existing.name(),
                existing.link(),
                existing.port(),
                existing.type(),
                state,
                counter,
                newInterval != null ? newInterval : existing.interval()
        );
        return monitoredRepository.save(updated);
    }

    public Monitored updateMonitored(String id, String ownerId, MonitoredDTO monitoredDTO) {

        Monitored existing = monFindByIdAndOwner(id, ownerId);

        Monitored updated = new Monitored(
                existing.id(),
                ownerId,
                monitoredDTO.name(),
                monitoredDTO.link(),
                monitoredDTO.port(),
                monitoredDTO.type(),
                existing.beingMonitored(),
                existing.monitoringStartTime(),
                monitoredDTO.interval() != null ? monitoredDTO.interval() : existing.interval()
        );
        return monitoredRepository.save(updated);
    }

    public void removeMonitored(String id, String ownerId) {
        Monitored existing = monFindByIdAndOwner(id, ownerId);
        monitoredRepository.delete(existing);
    }

    public List<MonStatusDTO> statusList(List<Monitored> userMonitored) {

        List<MonStatusDTO> status = new ArrayList<>();

        for (Monitored monitored : userMonitored) {
            if (!monitored.beingMonitored()) {

                MonStatusDTO dto = new MonStatusDTO(
                        monitored,
                        null
                );
                status.add(dto);
                continue;
            }

            Optional<LogEntry> log =  logRepository.findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc(monitored.ownerId(), monitored.id());

            Integer statusCode = log.map(LogEntry::statusCode).orElse(null);

            MonStatusDTO dto = new MonStatusDTO(
                    monitored,
                    statusCode
            );
            status.add(dto);

        }
        return status;
    }

    public Monitored toggleMonitored(String id, String ownerId, boolean state) {
        Monitored existing = monFindByIdAndOwner(id, ownerId);

        if (existing.beingMonitored() == state) {
            return existing;
        }

        Instant counter = state ? Instant.now() : null;

        Monitored toggled = new Monitored(
                existing.id(),
                ownerId,
                existing.name(),
                existing.link(),
                existing.port(),
                existing.type(),
                state,
                counter,
                existing.interval()
        );
        return monitoredRepository.save(toggled);
    }
}