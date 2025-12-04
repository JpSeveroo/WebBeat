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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

//6928c90fef163892b02ae0f1

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


    public SchedulerService(ThreadPoolTaskScheduler scheduler, LogRepository logRepository, MonitoredRepository monitoredRepository) {
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

    public void startScheduler(int delay) {
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
                    ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(delay));
                    tasks.put(monitored.id(), future);
                    LOG.info("task running");
                }
            }
        }
    }


    public void allowMonitoring(String ownerID, String taskID) {
        List<Monitored> allowed = new ArrayList<>() ;

        for (Monitored monitored : apis.values()) {
            if (monitored.beingMonitored()) {
                allowed.add(monitored);
            }
        }

        if (allowed.size() == 5) {
            LOG.info("There are more than 5 tasks to allow");
        }
        else {
            monitoredService.toggleMonitored(ownerID, taskID, true);
        }
    }

    public void removeMonitoring(String ownerID, String taskID) {
        monitoredService.toggleMonitored(ownerID, taskID, false);
    }

    public Integer getStatus(String ownerId, String monitoredId) {
        LogEntry log = logRepository.findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc(ownerId, monitoredId).orElse(null);
        return log.statusCode();
    }

    public void stopMonitoring() {
        for (Monitored monitored : apis.values()) {
            ScheduledFuture<?> future = tasks.get(monitored.id());
            if (future != null || future.isCancelled()) {
                LOG.info("Task not running");
            } else {
                future.cancel(true);
                tasks.remove(monitored.id());
                LOG.info("task stopped");
            }
        }
    }

    public void tasksStatus() {
        tasks.forEach((taskID, future) -> {
            System.out.println("Task " + taskID + " is running" + !future.isCancelled());
        });
    }
}
