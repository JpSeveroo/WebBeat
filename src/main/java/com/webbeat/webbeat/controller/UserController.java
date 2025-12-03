package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    UserService userService;

    @GetMapping("/settings")
    public String settingsPage(Model model) {
        model.addAttribute("activePage", "settings");

        return "settings";
    }
}