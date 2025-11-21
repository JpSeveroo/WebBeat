package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.dto.MonitoredDTO;
import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.security.CustomUserDetails;
import com.webbeat.webbeat.service.MonitoredService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/monitored")
public class MonitoredController {

    private final MonitoredService monitoredService;

    public MonitoredController(MonitoredService monitoredService) {
        this.monitoredService = monitoredService;
    }

    @GetMapping
    public String readMonitored(Model model, @AuthenticationPrincipal CustomUserDetails user) {

        var myMonitored = monitoredService.monFindByOwnerId(user.getId());

        model.addAttribute("monitored", myMonitored);

        return "allURLs";
    }

    @PostMapping("/add")
    public String postMonitored(@ModelAttribute MonitoredDTO monitoredDTO,
                                @AuthenticationPrincipal CustomUserDetails user) {

        monitoredService.registerNewMonitored(monitoredDTO, user.getId());

        return "redirect:/monitored";
    }

    @PostMapping("/delete/{id}")
    public String deleteMonitored(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        monitoredService.removeMonitored(id, user.getId());
        return "redirect:/monitored";
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable String id,
            Model model,
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        Monitored monitored = monitoredService.monFindByIdAndOwner(id, user.getId());

        MonitoredDTO monitoredDTO = new MonitoredDTO(monitored.name(), monitored.link());

        model.addAttribute("monitored", monitoredDTO);
        model.addAttribute("id", id);

        return "editURL";
    }

    @PostMapping("/update/{id}")
    public String updateMonitored(
            @PathVariable String id,
            @ModelAttribute MonitoredDTO monitoredDTO,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        monitoredService.updateMonitored(id, user.getId(), monitoredDTO);
        return "redirect:/monitored";
    }
}

















