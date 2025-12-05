package com.webbeat.webbeat.controller;

import com.webbeat.webbeat.dto.ChangePasswordDTO;
import com.webbeat.webbeat.security.CustomUserDetails;
import com.webbeat.webbeat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String settingsPage(Model model, @AuthenticationPrincipal CustomUserDetails user) {
        model.addAttribute("activePage", "settings");
        model.addAttribute("user", user);

        return "settings";
    }

    @GetMapping("/change-password")
    public String ChangePasswordSettingsGet(Model model, @AuthenticationPrincipal CustomUserDetails user) {
        model.addAttribute("activePage", "settings");
        model.addAttribute("user", user);
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO(null, null, null));

        return "change-password";
    }

    @PostMapping("/password")
    public String changePasswordSettingsPost(@AuthenticationPrincipal CustomUserDetails user,
                                             @Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO dto,
                                             BindingResult bindingResult,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "settings");
            model.addAttribute("user", user);
            return "settings";
        }

        try {
            userService.changePasswordSettingsPage(user.getId(), dto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/settings";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        return "redirect:/settings";
    }

    @PostMapping("/update-telegram")
    public String updateTelegram(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @org.springframework.web.bind.annotation.RequestParam String telegramChatId,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.updateTelegramSettings(userDetails.getId(), telegramChatId);
            com.webbeat.webbeat.model.User usuarioAtualizado = userService.findById(userDetails.getId());
            userDetails.setUser(usuarioAtualizado);
            redirectAttributes.addFlashAttribute("successMessage", "Telegram configurado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar: " + e.getMessage());
        }
        return "redirect:/settings";
    }
}