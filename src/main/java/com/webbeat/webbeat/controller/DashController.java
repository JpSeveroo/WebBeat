package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.dto.DashboardStatsDTO;
import com.webbeat.webbeat.security.CustomUserDetails;
import com.webbeat.webbeat.service.DashService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashController {

    private final DashService dashService;

    public DashController(DashService dashService) {
        this.dashService = dashService;
    }

    @GetMapping("/dashboard")
    public String dashboardPage(@AuthenticationPrincipal CustomUserDetails user ,Model model) {

        DashboardStatsDTO stats = dashService.getDashboardStats(user.getId());
        model.addAttribute("stats", stats);

        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}