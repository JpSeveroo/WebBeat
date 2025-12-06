package com.webbeat.webbeat.service;

import com.webbeat.webbeat.integration.TelegramIntegration;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    private final TelegramIntegration telegramIntegration;

    public TelegramService(TelegramIntegration telegramIntegration) {
        this.telegramIntegration = telegramIntegration;
    }

    public void notificarFalha(String targetChatId, String nomeServico, String link, String erro) {
        String texto = String.format("""
                🚨 *ALERTA DE FALHA* 🚨
                🔴 *Serviço:* %s
                🔗 *URL:* %s
                ⚠️ *Erro:* %s
                """, nomeServico, link, erro);

        telegramIntegration.enviarMensagemDireta(targetChatId, texto);
    }
    public void notificarRecuperacao(String targetChatId, String nomeServico, String link) {
        String texto = String.format("""
                ✅ *SERVIÇO RECUPERADO* ✅
                🟢 *Serviço:* %s
                🔗 *URL:* %s
                
                O sistema voltou a responder com sucesso!
                """, nomeServico, link);

        telegramIntegration.enviarMensagemDireta(targetChatId, texto);
    }
}