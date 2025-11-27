package com.webbeat.webbeat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashController {

    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {

        model.addAttribute("activePage", "dashboard");

        return "dashboard";
    }

    @GetMapping("/logout-success")
    public String logoutPage() {
        return "redirect:/auth/logout";
    }
}
