package com.webbeat.webbeat.service;

import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.MonitoredRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class MonitoringService {

    private final MonitoredRepository monitoredRepository;
    private final TelegramService telegramService;
    private final WebClient webClient;

    public MonitoringService(MonitoredRepository monitoredRepository, TelegramService telegramService) {
        this.monitoredRepository = monitoredRepository;
        this.telegramService = telegramService;
        // WebClient com timeout de 5 segundos
        this.webClient = WebClient.builder()
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(
                        reactor.netty.http.client.HttpClient.create().responseTimeout(Duration.ofSeconds(5))
                ))
                .build();
    }

    @Scheduled(fixedRate = 60000)
    public void verificarTodosServicos(){
        System.out.print("Iniciando a análise de serviços...");
        List<Monitored> servicos = monitoredRepository.findAll();// O foda é que aqui ele ta pegando tudo do dataBase, mas dps incrementa uma logica aqui ou no database;

        for (Monitored servico : servicos){
            verificarServico(servico);
        }
    }

    private void verificarServico(Monitored servico) {
        webClient.get()
                .uri(servico.link())
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> {
                    System.out.println("✅ " + servico.name() + " está Online.");
                })
                .doOnError(erro -> {
                    System.err.println("❌ FALHA EM: " + servico.name());
                    telegramService.AlertaNotification(servico.name(), servico.link(), erro.getMessage());
                })
                .subscribe();
        //Logica porca pra ajeitar depois
    }
}
