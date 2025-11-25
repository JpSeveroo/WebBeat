package com.webbeat.webbeat.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class TelegramIntegration {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_CHAT}")
    private String chatPadrao;

    private final WebClient webClient;

    public TelegramIntegration(WebClient telegramWebClient) {
        this.webClient = telegramWebClient;
    }

    public void enviarMensagem(String mensagem) {
        String path = String.format("/bot%s/sendMessage", botToken);

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("chat_id", chatPadrao)
                        .queryParam("text", mensagem)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> System.err.println("❌ Erro Telegram: " + e.getMessage()))
                .subscribe();
    }

    public void enviarMensagemDireta(String chatId, String mensagem) {
        String path = String.format("/bot%s/sendMessage", botToken);

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("chat_id", chatId) // Usa o ID de quem chamou
                        .queryParam("text", mensagem)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }

    public Mono<JsonNode> getUpdates(long offset) {
        String path = String.format("/bot%s/getUpdates", botToken);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("offset", offset) // Pega só mensagens novas
                        .queryParam("timeout", 10)    // Espera 10s se estiver vazio
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(e -> System.err.println("❌ Erro ao buscar updates: " + e.getMessage()));
    }
}
