package com.webbeat.webbeat.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TelegramIntegration {
    
    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_CHAT}")
    private String chatID;

    private final WebClient webClient;

    public TelegramIntegration(WebClient telegramWebClient) {
        this.webClient = telegramWebClient;
    }
}
