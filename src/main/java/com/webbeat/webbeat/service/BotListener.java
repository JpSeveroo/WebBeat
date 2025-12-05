package com.webbeat.webbeat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.webbeat.webbeat.integration.TelegramIntegration;
import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.model.User;
import com.webbeat.webbeat.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BotListener {
    private final TelegramIntegration telegramIntegration;
    private final UserRepository userRepository;      // NOVO
    private final MonitoredService monitoredService;  // NOVO

    private long lastUpdateId = 0;

    public BotListener(TelegramIntegration telegramIntegration,
                       UserRepository userRepository,
                       MonitoredService monitoredService) {
        this.telegramIntegration = telegramIntegration;
        this.userRepository = userRepository;
        this.monitoredService = monitoredService;
    }

    @Scheduled(fixedDelay = 2000)
    public void verificarComando() {
        JsonNode response = telegramIntegration.getUpdates(lastUpdateId + 1).block();

        if (response != null && response.has("result") && response.get("result").isArray()) {
            for (JsonNode update : response.get("result")) {
                processarUpdate(update);
                lastUpdateId = update.get("update_id").asLong();
            }
        }
    }

    private void processarUpdate(JsonNode update) {
        if (!update.has("message") || !update.get("message").has("text")) return;

        String texto = update.get("message").get("text").asText();
        String chatId = update.get("message").get("chat").get("id").asText();

        String nomeUsuario = update.get("message").get("from").has("first_name")
                ? update.get("message").get("from").get("first_name").asText()
                : "Viajante";

        if (texto.equals("/start")){
            comandoStart(chatId, nomeUsuario);
        } else if (texto.equals("/menu")) {
            comandoMenu(chatId);
        } else if (texto.equals("/status")){
            comandoStatus(chatId); // Agora chama a nova versão
        } else if (texto.equals("/suporte")){
            comandoSuporte(chatId);
        } else if (texto.equals("/documentacao")){
            comandoDocumentacao(chatId);
        } else if (texto.equals("/id")) {
            comandoMeuid(chatId);
        } else {
            telegramIntegration.enviarMensagemDireta(chatId, "🤔 Não entendi... Digite /menu para ver as opções.");
        }
    }

    private void comandoStatus(String chatId) {
        Optional<User> userOpt = userRepository.findByTelegramChatId(chatId);

        if (userOpt.isEmpty()) {
            telegramIntegration.enviarMensagemDireta(chatId,
                    "⚠️ *Conta não vinculada!* \n\nVocê ainda não configurou este Telegram na sua conta WebBeat.\nDigite /id para saber seu código e configure no painel do site.");
            return;
        }

        User user = userOpt.get();
        List<Monitored> servicos = monitoredService.monFindByOwnerId(user.id());

        if (servicos.isEmpty()) {
            telegramIntegration.enviarMensagemDireta(chatId,
                    "📂 *Seus Serviços*\n\nVocê ainda não cadastrou nenhum serviço para monitorar.");
            return;
        }

        StringBuilder relatorio = new StringBuilder();
        relatorio.append("📊 *Status dos Serviços (" + servicos.size() + ")*\n\n");

        for (Monitored servico : servicos) {
            String statusIcon = servico.beingMonitored() ? "🟢" : "🔴";
            String statusText = servico.beingMonitored() ? "Monitorando" : "Pausado";

            relatorio.append(String.format("%s *%s*\n", statusIcon, servico.name()));
            relatorio.append(String.format("🔗 %s\n", servico.link()));
            relatorio.append(String.format("⏱ %ds  |  Status: %s\n", servico.interval(), statusText));
            relatorio.append("───────────────\n");
        }

        telegramIntegration.enviarMensagemDireta(chatId, relatorio.toString());
    }

    private void comandoStart(String chatId, String nome){
        String msg = String.format("""
                *👋 Olá, %s!*
                
                Bem-vindo ao WebBeat!
                Eu sou seu assistente de monitoramento.
                
                Para ver a lista de comandos disponíveis:
                → /menu
                """, nome);
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

    private void comandoMenu(String chatId){
        String  msg = """
                📂 *Menu WebBeat*
                
                [💡] /status — Verifica seus serviços e status atual
                [💡] /id — Descubra seu Chat ID para configurar no site
                [💡] /suporte — Informações de contato e ajuda
                [💡] /documentacao — Acesse o guia completo
                """;
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

    private void comandoMeuid(String chatId) {
        String msg = String.format("""
                🆔 *Seu Telegram Chat ID é:*
                
                `%s`
                
                Copie este número e cole nas configurações do WebBeat para ativar seus alertas.
                """, chatId);
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

    private void comandoSuporte(String chatId){
        String msg = """
                🛠️ *Suporte Técnico*
                
                Precisa de ajuda? Entre em contato com nossa equipe:
                
                E-mail: webbeat.suporte@gmail.com
                Tempo médio de resposta: 24h úteis
                """;
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }

    private void comandoDocumentacao(String chatId){
        String msg = """
                📘 *Documentação Completa*
                
                Acesse o guia no GitHub:
                https://github.com/JpSeveroo/WebBeat/blob/main/README.md
                """;
        telegramIntegration.enviarMensagemDireta(chatId, msg);
    }
}