# Documentação Completa do `TelegramIntegration` -- WebBeat

------------------------------------------------------------------------

## 1. Pacote da Classe

``` java
package com.webbeat.webbeat.integration;
```

Organiza a classe dentro do módulo `integration`, responsável pela
comunicação externa.

------------------------------------------------------------------------

## 2. Importações

``` java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
```

-   **@Value** -- injeta valores do arquivo de configuração
    (application.properties ou variáveis de ambiente).
-   **@Component** -- registra a classe como bean gerenciado pelo
    Spring.
-   **WebClient** -- cliente HTTP reativo do Spring.
-   **Mono** -- representa uma resposta assíncrona de um único valor
    (Reactor).
-   **JsonNode** -- estrutura de árvore JSON para trabalhar com
    respostas do Telegram.

------------------------------------------------------------------------

## 3. Anotação `@Component`

``` java
@Component
public class TelegramIntegration {
```

Indica que a classe será inicializada automaticamente pelo Spring e
poderá ser injetada em outras classes.

------------------------------------------------------------------------

## 4. Injeção de Token e Chat Padrão

``` java
@Value("${TELEGRAM_BOT_TOKEN}")
private String botToken;

@Value("${TELEGRAM_CHAT}")
private String chatPadrao;
```

Esses valores vêm do ambiente:

-   `TELEGRAM_BOT_TOKEN` -- token do bot.
-   `TELEGRAM_CHAT` -- chat padrão para envio de mensagens automáticas.

`@Value` injeta valores externos direto nos campos.

------------------------------------------------------------------------

## 5. Atributo `WebClient`

``` java
private final WebClient webClient;
```

Cliente HTTP usado para chamar a API do Telegram.

### Injeção via construtor:

``` java
public TelegramIntegration(WebClient telegramWebClient) {
    this.webClient = telegramWebClient;
}
```

O Spring fornece uma instância pré-configurada de `WebClient`.

------------------------------------------------------------------------

## 6. Método `enviarMensagem(String mensagem)`

``` java
public void enviarMensagem(String mensagem) {
    String path = String.format("/bot%s/sendMessage", botToken);

    webClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path(path)
                    .queryParam("chat_id", chatPadrao)
                    .queryParam("text", mensagem)
                    .queryParam("parse_mode","Markdowm")
                    .build())
            .retrieve()
            .bodyToMono(String.class)
            .doOnError(e -> System.err.println("Erro Telegram: " + e.getMessage()))
            .subscribe();
}
```

### Explicação:

-   Monta o endpoint `/botTOKEN/sendMessage`.
-   Usa `webClient.get()` para montar a requisição.
-   `queryParam` envia os parâmetros exigidos pelo Telegram API.
-   `retrieve()` executa a chamada.
-   `bodyToMono(String.class)` converte a resposta para String.
-   `subscribe()` envia de forma assíncrona.
-   `doOnError()` imprime erros caso ocorram.

------------------------------------------------------------------------

## 7. Método `enviarMensagemDireta(String chatId, String mensagem)`

``` java
public void enviarMensagemDireta(String chatId, String mensagem) {
    String path = String.format("/bot%s/sendMessage", botToken);

    webClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path(path)
                    .queryParam("chat_id", chatId)
                    .queryParam("text", mensagem)
                    .queryParam("parse_mode","Markdowm")
                    .build())
            .retrieve()
            .bodyToMono(String.class)
            .subscribe();
}
```

Mesma lógica que `enviarMensagem`, porém:

-   Envia para *qualquer chat*, não apenas o padrão.
-   Usado pelo bot para responder usuários diretamente.

------------------------------------------------------------------------

## 8. Método `getUpdates(long offset)`

``` java
public Mono<JsonNode> getUpdates(long offset) {
    String path = String.format("/bot%s/getUpdates", botToken);

    return webClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path(path)
                    .queryParam("offset", offset)
                    .queryParam("timeout", 10)
                    .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnError(e -> System.err.println("Erro ao buscar updates: " + e.getMessage()));
}
```

### Explicação:

-   Consulta `/getUpdates` do Telegram, que retorna novas mensagens.
-   `offset` impede que mensagens antigas sejam lidas novamente.
-   `timeout=10` faz o servidor esperar até 10 segundos por novas
    mensagens (long polling).
-   Retorna `Mono<JsonNode>`, permitindo tratar a resposta de forma
    reativa.

------------------------------------------------------------------------

## 9. Funcionamento Geral da Integração

1.  O bot envia mensagens via `sendMessage`.
2.  O bot recebe atualizações via `getUpdates`.
3.  Toda comunicação é feita com `WebClient`, de forma assíncrona.
4.  O Spring injeta token e chat automaticamente.
5.  A classe serve como o "gateway" entre o backend e o Telegram.

------------------------------------------------------------------------

## 10. Símbolos Java importantes usados

-   **@Component** -- registra classe no Spring.
-   **@Value** -- injeta variáveis de ambiente.
-   **final** -- impede reatribuição da variável.
-   **this** -- referência à instância atual.
-   **String.format** -- concatenação formatada.
-   **-\>** -- expressão lambda.
-   **Mono** -- representa um valor assíncrono do Reactor.
-   **subscribe()** -- dispara a execução da requisição.

------------------------------------------------------------------------
