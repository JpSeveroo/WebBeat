package com.webbeat.webbeat.tasks;

import com.webbeat.webbeat.service.SchedulerService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Objects;

@Component
@Scope("prototype")
public class RequestTasks implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RequestTasks.class);
    private final WebClient webClient;

    @Setter
    private String url;

    @Getter
    private Integer statusCode;

    public RequestTasks(WebClient webClient, SchedulerService schedulerService) {
        this.webClient = webClient;
    }

    @Override
    public void run() {
        var request = webClient.get()
                .uri(this.url)
                .exchangeToMono(response -> Mono.just(response.statusCode().value()))
                .block();


        if (request != 200) {
            LOG.warn(String.format("Request for %s returned status code %d", url, request));
        }

        if (!Objects.equals(this.statusCode, request)) {
            this.statusCode = request;
        }

        LOG.info(this.statusCode.toString());
    }
}
