package com.webbeat.webbeat.service;

import org.springframework.stereotype.Service;
import com.webbeat.webbeat.integration.TelegramIntegration;
import reactor.core.publisher.Mono;

@Service
public class TelegramService {

    private final TelegramIntegration telegramFunction;
    // Link "Mágico" para adicionar o bot
    private final String BOT_LINK = "https://t.me/WebBeatBot?startgroup=true";

    public TelegramService(TelegramIntegration telegramFunction) {
        this.telegramFunction = telegramFunction;
    }

    // 1. Envia o link no privado (Sua ideia do "Primeira vez")
    public void enviarLinkDeConfiguracao(String chatIdPrivado) {
        String msg = "🤖 Olá! Para ativar o monitoramento em grupo, adicione-me clicando aqui:\n" + BOT_LINK;
        telegramFunction.enviarMensagem(chatIdPrivado, null, msg).subscribe();
    }

    // 2. Cria a seção (Retorna o ID para ser salvo no banco pela Pessoa 1)
    public Mono<Integer> criarSecaoMonitoramento(String grupoId, String nomeServico) {
        return telegramFunction.criarTopico(grupoId, nomeServico);
    }

    // 3. Apaga a seção (Usado quando deletar o monitoramento)
    public void apagarSecaoMonitoramento(String grupoId, Integer topicoId) {
        if (grupoId != null && topicoId != null) {
            telegramFunction.deletarTopico(grupoId, topicoId).subscribe();
        }
    }

    // 4. Notificação de Falha (Inteligente: Privado ou Grupo)
    public void notificarFalha(String nome, String link, String erro, String chatId, Integer topicoId) {
        String texto = String.format("🚨 *FALHA:* %s (%s)\nErro: %s", nome, link, erro);

        // Se topicoId for null, o integration manda como mensagem comum (Privado)
        // Se tiver topicoId, manda na seção correta (Comunidade)
        telegramFunction.enviarMensagem(chatId, topicoId, texto).subscribe();
    }
}