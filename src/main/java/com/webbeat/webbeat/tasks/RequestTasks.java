package com.webbeat.webbeat.tasks;

import com.webbeat.webbeat.model.LogEntry;
import com.webbeat.webbeat.repository.LogRepository;
import com.webbeat.webbeat.service.TelegramService; 
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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
    private final TelegramService telegramService;

    @Setter private String url;
    @Setter private String name;
    @Setter private Integer port;
    @Setter private String type;
    @Setter private String monitoredId;
    @Setter private String ownerId;

    @Setter private String telegramChatId;

    @Getter
    private Integer statusCode;

    public RequestTasks(WebClient webClient, LogRepository logRepository, TelegramService telegramService) {
        this.webClient = webClient;
        this.logRepository = logRepository;
        this.telegramService = telegramService;
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
            dispararAlerta(e.getMessage());
        }
        salvarLog(this.statusCode, System.currentTimeMillis() - start);
    }

    private void checkHTTP(){
        long start = System.currentTimeMillis();
        int currentStatus = 0;
        String erroMsg = null;

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
            erroMsg = "Status HTTP: " + currentStatus;
            LOG.warn("HTTP Status Error for {}: {}", url, currentStatus);
        } catch (WebClientRequestException e ) {
            currentStatus = 0;
            erroMsg = "Erro de Rede/DNS";
            LOG.warn("HTTP Network Error for {}: {}", url, e.getMessage());
        } catch (Exception e) {
            currentStatus = 500;
            erroMsg = "Erro desconhecido: " + e.getMessage();
            LOG.error("Unknown Error while monitoring {}: {}", url, e.getMessage());
        }

        if (currentStatus != 200 && erroMsg != null) {
            dispararAlerta(erroMsg);
        }

        long duration = System.currentTimeMillis() - start;
        this.statusCode = currentStatus;

        LOG.info("HTTP Check: {} | Status: {} | Time: {}ms", this.url, currentStatus, duration);
        salvarLog(this.statusCode, duration);
    }

    private void dispararAlerta(String erro) {
        if (this.telegramChatId != null && !this.telegramChatId.isEmpty()) {
            telegramService.notificarFalha(this.telegramChatId, this.name != null ? this.name : "Serviço", this.url, erro);
        }
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
