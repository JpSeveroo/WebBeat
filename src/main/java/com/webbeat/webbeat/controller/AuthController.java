package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.dto.UserDTO;
import com.webbeat.webbeat.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
            model.addAttribute("registerRequest", new UserDTO(null, null));
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserDTO request,
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

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordRequest(
            @RequestParam String email,
            RedirectAttributes redirectAttributes
    ) {
        userService.requestPasswordReset(email);

        redirectAttributes.addFlashAttribute("successMessage",
                "If an account exists for " + email + ", you will receive a reset link shortly.");

        return "redirect:/auth/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.completeResetPassword(token, newPassword);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Password reset successfully! Please login with your new credentials.");

            return "redirect:/auth/login";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/login";
        }
    }
}









