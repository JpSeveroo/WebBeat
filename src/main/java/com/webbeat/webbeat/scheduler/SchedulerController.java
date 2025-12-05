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

    @PostMapping("/start/{delay}")
    public void startMonitoring(@PathVariable Integer delay, @AuthenticationPrincipal CustomUserDetails user) {
        schedulerService.allApis(user.getId());
        schedulerService.startScheduler(delay);
    }

    @PostMapping("/stop")
    public void stopMonitoring() {
        schedulerService.stopMonitoring();
    }

    @PatchMapping("/allow/{id}")
    public void allow(@AuthenticationPrincipal CustomUserDetails user,
                      @PathVariable String id,
                      @RequestParam(defaultValue = "30") Integer delay) {

        schedulerService.allowMonitoring(user.getId(), id, delay);
    }

    @PatchMapping("/remove/{id}")
    public void remove(@AuthenticationPrincipal CustomUserDetails user, @PathVariable String id) {
        schedulerService.removeMonitoring(user.getId(), id);
    }

    @GetMapping("/status/{id}")
    public Integer getStatus(@AuthenticationPrincipal CustomUserDetails user, @PathVariable String id) {
        return schedulerService.getStatus(user.getId(), id);
    }

}
