package com.webbeat.webbeat.service;

import com.webbeat.webbeat.dto.MonitoredDTO;
import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.MonitoredRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoredService {

    private final MonitoredRepository monitoredRepository;

    public MonitoredService(MonitoredRepository monitoredRepository) {
        this.monitoredRepository = monitoredRepository;
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
            throw new IllegalStateException("This link is already being monitored by you");
        }

        Monitored newMonitored = new Monitored(
                null,
                ownerId,
                monitoredDTO.name(),
                monitoredDTO.link(),
                false,
                null
        );

        return monitoredRepository.save(newMonitored);
    }

    public Monitored updateMonitored(String id, String ownerId, MonitoredDTO monitoredDTO) {

        Monitored existing = monFindByIdAndOwner(id, ownerId);

        Monitored updated = new Monitored(
                existing.id(),
                ownerId,
                monitoredDTO.name(),
                monitoredDTO.link(),
                existing.beingMonitored(),
                existing.monitoringStartTime()

        );
        return monitoredRepository.save(updated);
    }

    public void removeMonitored(String id, String ownerId) {

        Monitored existing = monFindByIdAndOwner(id, ownerId);

        monitoredRepository.delete(existing);
    }

}
