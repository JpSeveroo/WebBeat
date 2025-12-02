package com.webbeat.webbeat.scheduler;

import com.webbeat.webbeat.model.LogEntry;
import com.webbeat.webbeat.security.CustomUserDetails;
import com.webbeat.webbeat.scheduler.SchedulerService;
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

    @PostMapping("/start/{id}")
    public void startMonitoring(@AuthenticationPrincipal CustomUserDetails user,  @PathVariable String id) {
        schedulerService.allApis(user.getId());
        schedulerService.startScheduler(id, 1);
    }

    @GetMapping("/status")
    public Integer getStatus(@AuthenticationPrincipal CustomUserDetails user, @PathVariable String taskID) {
        return schedulerService.getStatus(user.getId(), taskID);
    }

}
