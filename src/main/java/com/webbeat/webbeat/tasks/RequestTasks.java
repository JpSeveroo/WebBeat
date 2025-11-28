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

@Component
@Scope("prototype")
public class RequestTasks implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RequestTasks.class);
    private final WebClient webClient;
    private final SchedulerService schedulerService;

    @Setter
    private String url;

    @Getter
    private Integer statusCode;

    public RequestTasks(WebClient webClient, SchedulerService schedulerService) {
        this.webClient = webClient;
        this.schedulerService = schedulerService;
    }

    @Override
    public void run() {
        this.statusCode = webClient.get()
                .uri(this.url)
                .exchangeToMono(response -> Mono.just(response.statusCode().value()))
                .block();

        schedulerService.api_status.replace()

        if (this.statusCode != 200) {
            LOG.warn("status code not 200");
        }

        assert this.statusCode != null;
        LOG.info(this.statusCode.toString());
    }
}
