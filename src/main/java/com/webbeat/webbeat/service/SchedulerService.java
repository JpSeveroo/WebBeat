package com.webbeat.webbeat.service;

import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.MonitoredRepository;
import com.webbeat.webbeat.tasks.RequestTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ReactorResourceFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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

    private final MonitoredRepository monitoredRepository;

    private Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public Map<String, Monitored> apis = new ConcurrentHashMap<>();

    @Autowired
    private ObjectFactory<RequestTasks> tasksFactory;

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ReactorResourceFactory reactorResourceFactory;

    public SchedulerService(ThreadPoolTaskScheduler scheduler, MonitoredRepository monitoredRepository) {
        this.scheduler = scheduler;
        this.monitoredRepository = monitoredRepository;
    }

    public void allApis(String userId) {
        List<Monitored> allApis = monitoredRepository.findByOwnerId(userId);
        for (Monitored monitored : allApis) {
            apis.put(monitored.id(), monitored);
        }
    }

    public void startScheduler(String taskID, int delay) {
        LOG.info(taskID);

        var beingMonitored = true;

        Monitored monitored = apis.get(taskID);

        RequestTasks task = tasksFactory.getObject();
        task.setUrl(monitored.link());

        if (tasks.containsKey(taskID) && !tasks.get(taskID).isCancelled()) {
            System.out.println("Task " + taskID + " is already running");
        } else if (beingMonitored) {
            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(delay));
            tasks.put(taskID, future);
            LOG.info("task running");
        }
    }

    public void stopMonitoring(String taskID) {
        ScheduledFuture<?> future = tasks.get(taskID);
        if (future != null || future.isCancelled()) {
            System.out.println("Task not runnig");
        }

        future.cancel(true);
        tasks.remove(taskID);
    }

    public void tasksStatus() {
        tasks.forEach((taskID, future) -> {
            System.out.println("Task " + taskID + " is running" + !future.isCancelled());
        });
    }
}
