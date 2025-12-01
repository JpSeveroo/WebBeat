# Documentação Completa do `BotListener` -- WebBeat

------------------------------------------------------------------------

## 📦 Pacote da Classe

``` java
package com.webbeat.webbeat.service;
```

Define o pacote onde a classe está armazenada. Pacotes ajudam a
organizar o projeto.

------------------------------------------------------------------------

## 📥 Importações

``` java
import com.fasterxml.jackson.databind.JsonNode;
import com.webbeat.webbeat.integration.TelegramIntegration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
```

-   **JsonNode** --- representa nós JSON recebidos da API do Telegram.\
-   **TelegramIntegration** --- classe do seu projeto responsável por
    enviar/receber mensagens.\
-   **@Scheduled** --- permite agendar métodos para rodar
    periodicamente.\
-   **@Service** --- indica que esta classe é um componente de serviço
    do Spring.

------------------------------------------------------------------------

## 🧩 Anotação `@Service`

``` java
@Service
public class BotListener {
```

Marca a classe para que seja gerenciada pelo Spring como um *bean de
serviço*.\
Isso permite **injeção de dependência** e controle do ciclo de vida pelo
framework.

------------------------------------------------------------------------

## 🔧 Atributos da Classe

``` java
private final TelegramIntegration telegramIntegration;
private long lastUpdateId = 0;
```

-   `telegramIntegration` --- usado para comunicação com a API do
    Telegram.\
-   `lastUpdateId` --- guarda o último ID processado para evitar
    reprocessar mensagens.

`final` significa que a variável só pode ser atribuída **uma única vez**
(inicialização no construtor).

------------------------------------------------------------------------

## 🏗️ Construtor

``` java
public BotListener(TelegramIntegration telegramIntegration){
    this.telegramIntegration = telegramIntegration;
}
```

-   O Spring injeta automaticamente a dependência `TelegramIntegration`.
-   `this.telegramIntegration` diferencia o campo da classe do
    parâmetro.

------------------------------------------------------------------------

## ⏱️ Método Agendado: `verificarComando()`

``` java
@Scheduled(fixedDelay = 2000)
public void verificarComando(){
```

-   `@Scheduled(fixedDelay = 2000)` executa o método a cada **2
    segundos** após a última execução terminar.
-   Ideal para ficar consultando atualizações no Telegram.

### Lógica interna:

``` java
telegramIntegration.getUpdates(lastUpdateId + 1)
        .subscribe(response -> {
```

-   `getUpdates()` busca novas mensagens.\
-   `lastUpdateId + 1` garante que só busca mensagens **posteriores** à
    última processada.\
-   `.subscribe(...)` porque usa programação reativa.

### Processamento dos updates:

``` java
if (response.has("result") && response.get("result").isArray()) {
    for (JsonNode update : response.get("result")) {
        processarUpdate(update);
        lastUpdateId = update.get("update_id").asLong();
    }
}
```

------------------------------------------------------------------------

## 📩 Método `processarUpdate()`

``` java
private void processarUpdate(JsonNode update) {
```

### Verificação de existência de texto:

``` java
if (!update.has("message") || !update.get("message").has("text")) return;
```

### Extração dos dados importantes:

``` java
String texto = update.get("message").get("text").asText();
String chatId = update.get("message").get("chat").get("id").asText();
```

### Nome do usuário:

``` java
String nomeUsuario = update.get("message").get("from").has("first_name")
        ? update.get("message").get("from").get("first_name").asText()
        : "Viajante";
```

------------------------------------------------------------------------

## 🧠 Lógica de Comandos

Fluxo baseado em comparações `equals()`:

``` java
if (texto.equals("/start")){
    comandoStart(chatId, nomeUsuario);
} else if (texto.equals("/menu")) {
    comandoMenu(chatId);
} else if (texto.equals("/status")){
    comandoStatus(chatId, nomeUsuario);
} else if (texto.equals("/suporte")){
    comandoSuporte(chatId);
} else if (texto.equals("/documentacao")){
    comandoDocumentacao(chatId);
} else {
    telegramIntegration.enviarMensagemDireta(chatId, "🤔 Não entendi... Digite /menu para ver as opções.");
}
```

------------------------------------------------------------------------

## 📝 Métodos de Resposta

Cada método monta uma mensagem em **String** e utiliza:

``` java
telegramIntegration.enviarMensagemDireta(chatId, msg);
```

### `/start`

Mensagem de boas-vindas com `String.format`.

### `/menu`

Retorna a lista de comandos disponíveis.

### `/status`

Placeholder indicando construção.

### `/suporte`

Retorna e-mail da equipe.

### `/documentacao`

Retorna link para o README.

------------------------------------------------------------------------

## 📌 Símbolos importantes em Java utilizados no código

-   **private** --- acessível apenas dentro da classe.\
-   **final** --- impede reatribuição.\
-   **this** --- referência para a instância atual.\
-   \*\*@*\* --- indica anotação (Spring/Java).\
-   **? :** --- operador ternário.\
-   **return;** --- encerra método imediatamente.\
-   **String.format()** --- interpolação de variáveis.\
-   **Text Blocks (`"""`)** --- strings multilinhas.

------------------------------------------------------------------------

## 🧠 Resumo Geral

1.  A cada 2 segundos o bot consulta o Telegram.\
2.  Recebe a lista de updates.\
3.  Filtra apenas mensagens com texto.\
4.  Identifica o comando.\
5.  Executa o método correspondente.\
6.  Responde via integração com o Telegram.

------------------------------------------------------------------------

