package com.webbeat.webbeat.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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


    public Mono<String> enviarMensagem(String chatId, Integer threadId, String mensagem){
        String path = String.format("/bot%s/sendMessage", botToken);
        String destino = (chatId != null) ? chatId : chatPadrao;

        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path(path)
                            .queryParam("chat_id", destino)
                            .queryParam("text", mensagem);
                    if (threadId != null) builder.queryParam("message_thread_id", threadId);
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> System.err.println("❌ Erro Telegram: " + e.getMessage()));
    }

    // --- NOVOS MÉTODOS (ADICIONE DAQUI PARA BAIXO) ---

    // 1. CRIAÇÃO DE TÓPICO (Para quando o usuário cria o monitoramento)
    public Mono<Integer> criarTopico(String chatId, String nomeServico) {
        String path = String.format("/bot%s/createForumTopic", botToken);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("chat_id", chatId)
                        .queryParam("name", "📊 " + nomeServico) // Ex: 📊 API Pagamento
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class) // Recebe JSON para pegar o ID gerado
                .map(json -> json.get("result").get("message_thread_id").asInt())
                .doOnError(e -> System.err.println("❌ Erro ao criar tópico: " + e.getMessage()));
    }

    // 2. EXCLUSÃO DE TÓPICO (Para quando o usuário apaga o monitoramento)
    public Mono<Void> deletarTopico(String chatId, Integer threadId) {
        String path = String.format("/bot%s/deleteForumTopic", botToken);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("chat_id", chatId)
                        .queryParam("message_thread_id", threadId)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> System.err.println("❌ Erro ao deletar tópico: " + e.getMessage()));
    }
}
