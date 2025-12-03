package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.dto.DashboardStatsDTO;
import com.webbeat.webbeat.security.CustomUserDetails;
import com.webbeat.webbeat.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashController {

    private final ReportService reportService;

    // Construtor com injeção de dependência do ReportService
    public DashController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/dashboard/stats")
    @ResponseBody
    public DashboardStatsDTO getDashboardStats(@AuthenticationPrincipal CustomUserDetails user) {
        String userId = user.getId();

        return reportService.getDashboardStats(userId);
    }
}