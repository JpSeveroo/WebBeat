package com.webbeat.webbeat.tasks;

import com.webbeat.webbeat.model.LogEntry;
import com.webbeat.webbeat.repository.LogRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;

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
        int currentStatus = 0;

        try {

            var responseEntity = webClient.get()
                    .uri(this.url)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            if (responseEntity != null) {
                currentStatus = responseEntity.getStatusCode().value();
            }

        } catch (WebClientResponseException e) {
            currentStatus = e.getStatusCode().value();
            LOG.warn("HTTP Status Error for {}: {}", url, currentStatus);
        } catch (WebClientRequestException e ) {
            currentStatus = 0;
            LOG.warn("HTTP Network Error for {}: {}", url, e.getMessage());
        } catch (Exception e) {
            currentStatus = 500;
            LOG.error("Unknown Error while monitoring {}: {}", url, e.getMessage());
        }

        long duration = System.currentTimeMillis() - start;
        this.statusCode = currentStatus;

        LOG.info("HTTP Check: {} | Status: {} | Time: {}ms", this.url, currentStatus, duration);
        salvarLog(this.statusCode, duration);
    }


    private void salvarLog(int status, long timeMs) {
        if (monitoredId == null || ownerId == null){
            return;
        };
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
