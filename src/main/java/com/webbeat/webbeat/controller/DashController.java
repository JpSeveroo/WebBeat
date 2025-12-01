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
@RequestMapping("/dashboard")
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

    // Método /settings consolidado
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        model.addAttribute("activePage", "settings");
        return "settings";
    }

    /**
     * Endpoint API REST para retornar as estatísticas do Dashboard em JSON.
     */
    @GetMapping("/dashboard/stats")
    @ResponseBody
    public DashboardStatsDTO getDashboardStats(@AuthenticationPrincipal CustomUserDetails user) {
        // Extração segura do userId (chave multi-tenant)
        String userId = user.getId();

        // Chama o serviço para calcular as métricas filtradas
        return reportService.getDashboardStats(userId);
    }

    @GetMapping("/logout-success")
    public String logoutPage() {
        return "redirect:/auth/logout";
    }
}