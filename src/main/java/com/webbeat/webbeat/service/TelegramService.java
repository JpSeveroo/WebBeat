package com.webbeat.webbeat.service;

import org.springframework.stereotype.Service;

import com.webbeat.webbeat.integration.TelegramIntegration;

@Service
public class TelegramService {

    private final TelegramIntegration telegramFunction;

    public TelegramService(TelegramIntegration telegramFunction) {
        this.telegramFunction = telegramFunction;
    }

    public void envioMensagem(String mensagem) {
        telegramFunction.enviarMensagem(mensagem)
                .doOnSuccess(response -> System.out.println("✅ Mensagem enviada com sucesso!"))
                .subscribe();
    }
    //aqui é só o cara usar TelegramService.envioMensagem("O site parou de funcionar viss"), exemplo.
}