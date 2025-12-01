# 📘 Guia Técnico: Módulo de Telegram do WebBeat

Este documento detalha o funcionamento interno da integração com o Telegram. Ele explica cada classe, anotação e conceito utilizado, servindo como referência para manutenção e estudos futuros.

---

# 1. `TelegramConfig.java` — A Fábrica de Ferramentas

**Localização:**  
`src/main/java/com/webbeat/webbeat/config/TelegramConfig.java`

Esta classe configura o cliente HTTP que será usado por toda a aplicação.

```java
@Configuration // [1]
public class TelegramConfig {

    @Bean // [2]
    public WebClient telegramWebClient() { // [3]
        return WebClient.builder()
                .baseUrl("https://api.telegram.org") // [4]
                .build();
    }
}
```

### 🧠 Decifrando o Código:

| Marcador | Explicação |
|---------|------------|
| **[1] @Configuration** | Diz ao Spring: “Leia esta classe ao iniciar, ela cria objetos importantes”. |
| **[2] @Bean** | Diz ao Spring que o retorno do método deve ser gerenciado como um Bean. |
| **[3] WebClient** | Cliente HTTP moderno, reativo e assíncrono do Spring. |
| **[4] .baseUrl(...)** | Define a URL base das requisições ao Telegram. |

---

# 2. `TelegramIntegration.java` — O Operário de Rede

**Localização:**  
`src/main/java/com/webbeat/webbeat/integration/TelegramIntegration.java`

Esta classe envia requisições HTTP ao Telegram.  
Ela sabe *como* falar com o Telegram, mas não *o que* falar.

```java
@Component // [1]
public class TelegramIntegration {

    @Value("${TELEGRAM_BOT_TOKEN}") // [2]
    private String botToken;

    // ... (Construtor injetando o WebClient) ...

    public void enviarMensagem(String mensagem) {
        String path = String.format("/bot%s/sendMessage", botToken); // [3]

        webClient.get()
                .uri(...) // Constrói a URL com parâmetros (?chat_id=...&text=...)
                .retrieve() // [4]
                .bodyToMono(String.class) // [5]
                .subscribe(); // [6]
    }
    
    public Mono<JsonNode> getUpdates(long offset) { ... } // [7]
}
```

### 🧠 Decifrando o Código:

| Marcador | Explicação |
|---------|------------|
| **[1] @Component** | Spring irá gerenciar essa classe como um componente. |
| **[2] @Value** | Lê variáveis do `.env` sem deixar tokens no código. |
| **[3] URL com Token** | O Telegram exige `/bot<TOKEN>/...` nos endpoints. |
| **[4] .retrieve()** | Dispara requisição e recebe resposta. |
| **[5] Mono** | Um valor futuro (0 ou 1). Reativo. |
| **[6] .subscribe()** | Inicia a operação assíncrona. |
| **[7] Mono<JsonNode>** | Permite que outra classe decida como processar o retorno. |

---

# 3. `TelegramService.java` — O Gerente de Conteúdo

**Localização:**  
`src/main/java/com/webbeat/webbeat/service/TelegramService.java`

Contém a regra de negócio.  
Formata textos bonitos, com emojis e Markdown.

```java
@Service
public class TelegramService {
    // ...
    public void notificarFalha(...) {
        String texto = """
                🚨 *ALERTA* 🚨
                ...
                """;
        telegramIntegration.enviarMensagem(texto);
    }
}
```

---

# 4. `BotListener.java` — O Porteiro (Recebimento)

**Localização:**  
`src/main/java/com/webbeat/webbeat/service/BotListener.java`

Responsável por fazer *polling* e buscar novas mensagens enviadas ao bot.

```java
@Service
public class BotListener {

    private long lastUpdateId = 0; // [1]

    @Scheduled(fixedDelay = 2000) // [2]
    public void verificarComando() {

        JsonNode response = telegramIntegration.getUpdates(lastUpdateId + 1).block(); // [3]

        if (response != null ...) {
            for (JsonNode update : response.get("result")) {
                processarUpdate(update);
                lastUpdateId = update.get("update_id").asLong(); // [4]
            }
        }
    }
}
```

### 🧠 Decifrando o Código:

| Marcador | Explicação |
|---------|------------|
| **[1] lastUpdateId** | Marca até onde as mensagens já foram lidas. |
| **[2] @Scheduled(fixedDelay = 2000)** | Executa o método a cada 2 segundos. |
| **[3] .block()** | Espera a resposta antes de continuar (evita corridas). |
| **[4] Atualiza o Offset** | Evita processar mensagens repetidas. |

---

# 📚 Glossário de Termos

| Termo | Definição |
|-------|-----------|
| **Bean** | Objeto criado e gerenciado pelo Spring. |
| **Injeção de Dependência** | Spring entrega automaticamente objetos prontos às classes. |
| **Endpoint** | Endereço específico da API (ex.: `/sendMessage`). |
| **JsonNode** | Representação flexível de um JSON (árvore de dados). |
| **Mono** | Representa um valor futuro (reativo). |
| **Subscribe** | Inicia uma operação reativa. |
| **Block** | Torna a operação síncrona, esperando a resposta. |
| **Polling** | Técnica de perguntar repetidamente por novidades. |

