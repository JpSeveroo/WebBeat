package com.webbeat.webbeat.service;

//import com.fasterxml.jackson.databind.JsonNode;
//import com.webbeat.webbeat.integration.TelegramIntegration;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;

//@Service
//public class BotListener {
//    private final TelegramIntegration telegramIntegration;
//    private long lastUpdateId = 0;
//
//    public BotListener(TelegramIntegration telegramIntegration){
//        this.telegramIntegration = telegramIntegration;
//    }
//
//    @Scheduled(fixedDelay = 2000)
//    public void verificarComando(){
//        telegramIntegration.getUpdates(lastUpdateId + 1)
//                .subscribe(response -> {
//                    if (response.has("result") && response.get("result").isArray()) {
//                        for (JsonNode update : response.get("result")) {
//                            processarUpdate(update);
//                            lastUpdateId = update.get("update_id").asLong();
//                        }
//                    }
//                });
//    }
//
//    private void processarUpdate(JsonNode update) {
//        if (!update.has("message") || !update.get("message").has("text")) return;
//
//        String texto = update.get("message").get("text").asText();
//        String chatId = update.get("message").get("chat").get("id").asText();
//        String nomeUsuario = update.get("message").get("from").get("first_name").asText();
//
//        // Lógica dos Comandos
//        if (texto.equals("/start")) {
//            String resposta = String.format("👋 Olá, %s! Bem-vindo ao WebBeat.\nEu sou seu assistente de monitoramento.\nUse /help para verificar minha documentação e ficar por dentro de tudo ou dê um /status para identificar o status de monitoramento de suas APIs.", nomeUsuario);
//            telegramIntegration.enviarMensagemDireta(chatId, resposta);
//        }
//        else if (texto.equals("/help")) {
//            String resposta = """
//                   Completar o /help depois
//                    """;
//            telegramIntegration.enviarMensagemDireta(chatId, resposta);
//        }
//        else if (texto.equals("/status")) {
//            telegramIntegration.enviarMensagemDireta(chatId, "🔍 Tudo operando normalmente! (Dados simulados)");
//        }
//        else {
//            telegramIntegration.enviarMensagemDireta(chatId, "🤔 Não entendi... Tente usar /help");
//        }
//    }
//
//}
