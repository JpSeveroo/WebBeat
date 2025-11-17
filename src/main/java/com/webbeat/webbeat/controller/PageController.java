package com.webbeat.webbeat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/logout-success")
    public String logoutPage() {
        return "redirect:/auth/logout";
    }
}
