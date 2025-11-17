package com.webbeat.webbeat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

//Criando um webclient específico para o telegram
@Configuration
public class  TelegramConfig {
    @Bean
    public WebClient telegramWebClient(){
        return WebClient.builder()
                .baseUrl("https://api.telegram.org")
                .build();
    }
}
