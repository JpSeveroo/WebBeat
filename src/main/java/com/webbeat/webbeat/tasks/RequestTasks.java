package com.webbeat.webbeat.tasks;

import com.webbeat.webbeat.model.LogEntry;
import com.webbeat.webbeat.repository.LogRepository;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

@Component
@Scope("prototype")
public class RequestTasks implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RequestTasks.class);
    private final WebClient webClient;
    private final LogRepository logRepository;

    @Setter private String url;
    @Setter private Integer port;
    @Setter private String type;
    @Setter private String monitoredId;
    @Setter private String ownerId;

    @Getter
    private Integer statusCode;

    public RequestTasks(WebClient webClient, LogRepository logRepository) {
        this.webClient = webClient;
        this.logRepository = logRepository;
    }

    @Override
    public void run() {
        if ("TCP".equalsIgnoreCase(this.type)) {
            LOG.info("Starting TCP Request");
            checkTCP();
        } else {
            LOG.info("Starting HTTP Request");
            checkHTTP();
        }
    }

    private void checkTCP(){
        long start = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(this.url, this.port), 3000);
            this.statusCode = 200;
            LOG.info("TCP OK: {}:{}", this.url, this.port);
        } catch (IOException e){
            this.statusCode = 503;
            LOG.warn("TCP FALHA: {}:{} - {}", this.url, this.port, e.getMessage());
        }
    }

    private void checkHTTP(){
        long start = System.currentTimeMillis();

        var request = webClient.get()
                    .uri(this.url)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode().value())
                    .block();
        if  (Objects.nonNull(request)) {
            long duration = System.currentTimeMillis() - start;
            LOG.info("HTTP OK: {} | Status: {} | ResponseTime: {}", this.url, request,  duration);

            if (request != 200) {
                LOG.warn("Request for {} returned status code {}", url, request);
            }

            if (!Objects.equals(this.statusCode, request)) {
                this.statusCode = request;
            }

            salvarLog(this.statusCode, duration);

            LOG.info(this.statusCode.toString());

        }
        else {
            LOG.info("Erro na requisição");
        }
    }


    private void salvarLog(int status, long timeMs) {
        if (monitoredId != null && ownerId != null) return;
        LogEntry log = new LogEntry(
                null,
                ownerId,
                monitoredId,
                Instant.now(),
                status,
                timeMs
        );
        logRepository.save(log);
    }
}
