package com.webbeat.webbeat.scheduler;

import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.MonitoredRepository;
import com.webbeat.webbeat.tasks.RequestTasks;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class SchedulerService {
    private final ThreadPoolTaskScheduler scheduler;
    private final MonitoredRepository monitoredRepository;
    private Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    @Autowired
    private ObjectFactory<RequestTasks> tasksFactory;

    public SchedulerService(ThreadPoolTaskScheduler scheduler, MonitoredRepository monitoredRepository) {
        this.scheduler = scheduler;
        this.monitoredRepository = monitoredRepository;
    }

    public String startScheduler(String taskID, int delay) {

        Optional<Monitored> url = monitoredRepository.findById(taskID);
        Monitored monitored = url.orElseThrow(() -> new RuntimeException("URL not found"));

        RequestTasks task = tasksFactory.getObject();
        task.setUrl(monitored.link());

        if (tasks.containsKey(taskID) && !tasks.get(taskID).isCancelled()) {
            System.out.println("Task " + taskID + " is already running");
        }

        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(delay));
        tasks.put(taskID, future);
        return "Task running";
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
