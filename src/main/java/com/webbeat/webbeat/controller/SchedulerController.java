package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.service.SchedulerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/start/{id}")
    public String startMonitoring(@PathVariable String id) {
        return schedulerService.startScheduler(id, 30);
    }
}
