package com.webbeat.webbeat.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class TelegramIntegration {
    
    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_CHAT}")
    private String chatID;

    private final WebClient webClient;

    /*Injeção de dependência. To injetando um construtor pra facilitar a organização
    do webClient que faz requisição http pra api do telegram*/

    public TelegramIntegration(WebClient telegramWebClient) {
        this.webClient = telegramWebClient;
    }

    public Mono<String> enviarMensagem(String mensagem){

        String path = String.format("/bot%s/sendMessage", botToken);

        return webClient.get()
            //Aqui eu monto a Url para a requisição (Sim, na tora)
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("chat_id", chatID)
                        .queryParam("text", mensagem)
                        .build())
            //Enviando a requisição http    
                .retrieve()
                .bodyToMono(String.class)
            //Se der merda retorna um erro no console
                .doOnError(e -> System.err.println("❌ Erro ao enviar mensagem para o Telegram: " + e.getMessage()));
    }

}
