package com.webbeat.webbeat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        model.addAttribute("activePage", "settings");

        return "settings";
    }
}