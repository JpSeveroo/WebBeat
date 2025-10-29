
package com.webbeat.webbeat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webbeat.webbeat.service.TelegramService;

/*Isso só serve pra testar se o bot ta dando aquele salve*/
@RestController
public class TelegramTestController {

    //Crianda essa desgraça de construtor
    private final TelegramService telegramService;

    public TelegramTestController(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    //Criando um pathzinho pra o teste
    @GetMapping("/test-telegram")
    public String testTelegram() {
        telegramService.envioMensagem("🚨 Alerta de teste: integração 2.0 funcionando!");
        return "Mensagem enviada! Verifique seu Telegram.";
    }
    
}
