package com.webbeat.webbeat.service;

//import com.webbeat.webbeat.integration.TelegramIntegration;
//import org.springframework.stereotype.Service;

//@Service
//public class TelegramService {
//
//    private final TelegramIntegration telegramIntegration;
//
//    public TelegramService(TelegramIntegration telegramIntegration) {
//        this.telegramIntegration = telegramIntegration;
//    }
//
//    public void notificarFalha(String nomeServico, String link, String erro) {
//        String texto = String.format("""
//                🚨 *ALERTA DE FALHA* 🚨
//
//                🔴 *Serviço:* %s
//                🔗 *URL:* %s
//                ⚠️ *Erro:* %s
//                """, nomeServico, link, erro);
//
//        telegramIntegration.enviarMensagem(texto);
//    }
//}