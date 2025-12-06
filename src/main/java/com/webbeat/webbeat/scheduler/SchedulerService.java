package com.webbeat.webbeat.scheduler;

import com.webbeat.webbeat.model.LogEntry;
import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.LogRepository;
import com.webbeat.webbeat.repository.MonitoredRepository;
import com.webbeat.webbeat.service.MonitoredService;
import com.webbeat.webbeat.tasks.RequestTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.client.ReactorResourceFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import com.webbeat.webbeat.repository.UserRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class SchedulerService {

    private final ThreadPoolTaskScheduler scheduler;
    private final LogRepository logRepository;
    private final MonitoredRepository monitoredRepository;
    private Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    public Map<String, Monitored> apis = new ConcurrentHashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

    private MonitoredService monitoredService;

    @Autowired
    private ReactorResourceFactory reactorResourceFactory;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ObjectFactory<RequestTasks> tasksFactory;
    @Autowired
    private UserRepository userRepository;


    public SchedulerService(ThreadPoolTaskScheduler scheduler, LogRepository logRepository, MonitoredRepository monitoredRepository, MonitoredService monitoredService) {
        this.monitoredService = monitoredService;
        this.scheduler = scheduler;
        this.logRepository = logRepository;
        this.monitoredRepository = monitoredRepository;
    }

    public void allApis(String userId) {
        List<Monitored> allApis = monitoredRepository.findByOwnerId(userId);
        if (allApis.isEmpty()) {
            LOG.warn("No apis found for user {}", userId);
        }
        else {
            for (Monitored monitored : allApis) {
                apis.put(monitored.id(), monitored);
            }
        }
    }

    public void startScheduler(int defaultDelay) {
        for (Monitored monitored : apis.values()) {
            if (monitored.beingMonitored()) {
                if (tasks.containsKey(monitored.id()) && !tasks.get(monitored.id()).isCancelled()) {
                    System.out.println("Task " + monitored.id() + " is already running");
                } else {
                    LOG.info("Starting scheduler for {}", monitored.name());
                    RequestTasks task = tasksFactory.getObject();
                    task.setOwnerId(monitored.ownerId());
                    task.setMonitoredId(monitored.id());
                    task.setUrl(monitored.link());
                    task.setPort(monitored.port());
                    task.setType(monitored.type());

                    int actualDelay = (monitored.interval() != null) ? monitored.interval() : defaultDelay;

                    ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(actualDelay));
                    tasks.put(monitored.id(), future);
                    LOG.info("task running");
                }
            }
        }
    }


    public void allowMonitoring(String ownerID, String taskID, Integer delay) {

        long activeCount = monitoredRepository.findByOwnerId(ownerID).stream()
                .filter(Monitored::beingMonitored)
                .count();

        if (activeCount >= 5) {
            throw new IllegalStateException("Limit reached! You can only monitor 5 services simultaneously.");
        }

        Monitored monitored = monitoredService.updateStatusAndInterval(taskID, ownerID, true, delay);

        apis.put(monitored.id(), monitored);

        int finalDelay = (delay != null) ? delay : (monitored.interval() != null ? monitored.interval() : 30);

        startSingleTask(monitored.id(), finalDelay);
    }

    public void removeMonitoring(String ownerID, String taskID) {
         Monitored monitored = monitoredService.toggleMonitored(taskID, ownerID, false);

        apis.put(monitored.id(), monitored);

        if (tasks.containsKey(taskID)) {
            ScheduledFuture<?> future = tasks.get(taskID);
            if (future != null) {
                future.cancel(true);
            }
            tasks.remove(taskID);
            LOG.info("Task {} stopped manually via removeMonitoring", taskID);
        }
    }

    public Integer getStatus(String ownerId, String monitoredId) {
        LogEntry log = logRepository.findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc(ownerId, monitoredId).orElse(null);
        return log.statusCode();
    }

    public void stopMonitoring() {
        for (String monitoredId : tasks.keySet()) {
            ScheduledFuture<?> future = tasks.get(monitoredId);
            if (future != null) {
                future.cancel(true);
                LOG.info("Task {} stopped (Global Stop)", monitoredId);
            }
        }
        tasks.clear();
    }

    public void startSingleTask(String monitoredId, int delaySeconds) {

        Monitored monitored = apis.get(monitoredId);

        if (monitored == null) {
            monitored = monitoredRepository.findById(monitoredId).orElse(null);
            if (monitored != null) apis.put(monitored.id(), monitored);
        }

        if (monitored != null) {
            if (tasks.containsKey(monitoredId)) {
                ScheduledFuture<?> existing = tasks.get(monitoredId);
                if (existing != null && !existing.isCancelled()) {
                    existing.cancel(true);
                }
            }

            LOG.info("Starting isolated monitoring for {} with {}s of delay", monitored.name(), delaySeconds);

            RequestTasks task = tasksFactory.getObject();
            task.setOwnerId(monitored.ownerId());
            task.setMonitoredId(monitored.id());
            task.setUrl(monitored.link());
            task.setPort(monitored.port());
            task.setType(monitored.type());
            task.setName(monitored.name());

            var dono = userRepository.findById(monitored.ownerId()).orElse(null);
            if (dono != null) {
                task.setTelegramChatId(dono.telegramChatId());
            }

            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(delaySeconds));
            tasks.put(monitored.id(), future);
        }
    }
}
