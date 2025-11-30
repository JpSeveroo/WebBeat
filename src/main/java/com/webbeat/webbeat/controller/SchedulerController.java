package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.security.CustomUserDetails;
import com.webbeat.webbeat.service.SchedulerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/start")
    public String startMonitoring(@AuthenticationPrincipal CustomUserDetails user) {
        return schedulerService.startScheduler(user.getId(), 1);
    }

    @GetMapping("/status")
    public String getStatus(@PathVariable String taskID) {
        return schedulerService.getStatus(taskID);
    }

}
