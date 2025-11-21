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
        telegramFunction.enviarMensagem(mensagem);
    }

    public void AlertaNotification(String nomeServico, String url, String erro){
        String mensagemFormatada = String.format(
                """
                🚨 *ALERTA DE FALHA DETECTADA* 🚨
                
                📉 *Serviço:* %s
                🔗 *URL:* %s
                ❌ *Erro:* %s
                
                Verifique imediatamente!
                """,
                nomeServico, url, erro);

        telegramFunction.enviarMensagem(mensagemFormatada).subscribe();
    }
}