package com.webbeat.webbeat.controller;


import com.webbeat.webbeat.dto.RegisterRequest;
import com.webbeat.webbeat.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {

        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest(null, null));
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequest request,
                               RedirectAttributes redirectAttributes) {
        try {

            userService.registerNewUser(request);

            redirectAttributes.addFlashAttribute("successMessage", "User registered successfully");

            return "redirect:/auth/login";
        } catch (IllegalStateException e) {

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("registerRequest", request);

            return "redirect:/auth/register";
        }
    }
}









