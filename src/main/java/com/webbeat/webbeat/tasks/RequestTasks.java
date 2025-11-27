package com.webbeat.webbeat.tasks;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Scope("prototype")
public class RequestTasks implements Runnable {

    private final WebClient webClient;

    @Setter
    private String url;

    @Getter
    private Integer statusCode;

    public RequestTasks(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public void run() {
        this.statusCode = webClient.get()
                .uri(this.url)
                .exchangeToMono(response -> Mono.just(response.statusCode().value()))
                .block();
        System.out.println(this.statusCode);
    }
}
