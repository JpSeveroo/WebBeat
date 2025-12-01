package com.webbeat.webbeat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.webbeat.webbeat.integration.TelegramIntegration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BotListener {
    private final TelegramIntegration telegramIntegration;
    private long lastUpdateId = 0;

    public BotListener(TelegramIntegration telegramIntegration){
        this.telegramIntegration = telegramIntegration;
    }

    @Scheduled(fixedDelay = 2000)
    public void verificarComando() {
        JsonNode response = telegramIntegration.getUpdates(lastUpdateId + 1).block();

        if (response != null && response.has("result") && response.get("result").isArray()) {
            for (JsonNode update : response.get("result")) {
                processarUpdate(update);
                lastUpdateId = update.get("update_id").asLong();
            }
        }
    }

    private void processarUpdate(JsonNode update) {
        if (!update.has("message") || !update.get("message").has("text")) return;

        String texto = update.get("message").get("text").asText();
        String chatId = update.get("message").get("chat").get("id").asText();

        String nomeUsuario = update.get("message").get("from").has("first_name")
                ? update.get("message").get("from").get("first_name").asText()
                : "Viajante";

        if (texto.equals("/start")){
            comandoStart(chatId, nomeUsuario);
        } else if (texto.equals("/menu")) {
            comandoMenu(chatId);
        } else if (texto.equals("/status")){
            comandoStatus(chatId, nomeUsuario);
        }else if (texto.equals("/suporte")){
            comandoSuporte(chatId);
        }else if (texto.equals("/documentacao")){
            comandoDocumentacao(chatId);
        }else {
            telegramIntegration.enviarMensagemDireta(chatId, "🤔 Não entendi... Digite /menu para ver as opções.");
        }
    }

    private void comandoStart(String chatId, String nome){
        String msg = String.format("""
                *👋 Olá, %s!*
                
                Bem-vindo ao WebBeat!
                
                Eu sou seu assistente de monitoramento. A partir de agora, sempre que uma API ou porta ficar indisponível, você será notificado automaticamente.
                
                Para ver a lista de comandos disponíveis:
                → /menu
                """, nome);
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

    private void comandoMenu(String chatId){
        String  msg = """
                📂 *Menu WebBeat*
                
                [💡] /status — Verifica o estado atual dos serviços monitorados \s
                [💡] /suporte — Informações de contato e ajuda \s
                [💡] /documentacao — Acesse o guia completo da aplicação
                """;
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

    private void comandoStatus(String chatId, String nome){
        String msg = String.format("""
                Em construcao...😭😭😭😭😭😭😭😭😭
                """);
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

    private void comandoSuporte(String chatId){
        String msg = """
                🛠️ *Suporte Técnico*
                
                Precisa de ajuda? Entre em contato com nossa equipe:
                
                E-mail: webbeat.suporte@gmail.com \s
                Tempo médio de resposta: 24h úteis
                
                Estamos aqui para ajudar.
                
                """;
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

    private void comandoDocumentacao(String chatId){
        String msg = """
                📘 *Documentação Completa*
                
                Você pode acessar toda a documentação, exemplos e instruções de uso no link abaixo:
                
                *GitHub (README):* https://github.com/JpSeveroo/WebBeat/blob/main/README.md
                """;
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

}
