# 📘 Guia Técnico: Classe `RequestTasks` — WebBeat

A classe `RequestTasks` representa uma tarefa executada pelo agendador.  
Ela é responsável por testar serviços HTTP ou TCP e registrar o status da verificação.

Este documento explica **cada parte do código**, linha por linha, exceto o método de salvar log (`salvarLog()`), pois ele está **em construção** e não será detalhado.
---

# 🧠 EXPLICAÇÃO COMPLETA DO CÓDIGO

## 📦 Pacote
```java
package com.webbeat.webbeat.tasks;
```
Define onde a classe está localizada no projeto.

---

# 📥 Imports essenciais

### Classes do seu sistema
- `LogEntry` → representa um registro de log.
- `LogRepository` → usado para salvar o log no banco.
- `SchedulerService` → provavelmente cria/agenda tarefas (não usado aqui diretamente).

### Lombok
- `@Getter` e `@Setter` → geram automaticamente getters e setters.

### Spring
- `@Component` → permite que o Spring gerencie esse objeto.
- `@Scope("prototype")` → **cria uma nova instância da classe a cada uso**, importante porque cada tarefa tem dados diferentes.

### WebClient
Usado para fazer requisições HTTP.

### Java networking
- `Socket`, `InetSocketAddress`, `IOException` → usados no teste TCP.

### Datas
- `Instant` → momento exato do log.

---

# 🏷️ Anotações importantes da classe

```java
@Component
@Scope("prototype")
public class RequestTasks implements Runnable {
```

### `@Component`
O Spring reconhece essa classe como um componente que pode ser injetado.

### `@Scope("prototype")`
Cada vez que o Spring precisar de `RequestTasks`, ele criará **uma instância nova**.  
Isso evita conflitos entre tarefas correndo em paralelo.

### `implements Runnable`
Isso permite que a classe seja executada por threads (Scheduler, ExecutorService etc).

---

# 🔧 Atributos internos

```java
private static final Logger LOG = LoggerFactory.getLogger(RequestTasks.class);
```
Logger para mandar mensagens para o console.

---

### Dependências obrigatórias

```java
private final WebClient webClient;
private final LogRepository logRepository;
```

Esses objetos chegam pelo construtor (Injeção de Dependência).

---

### Parâmetros configuráveis (com @Setter)

```java
@Setter private String url;
@Setter private Integer port;
@Setter private String type;
@Setter private String monitoredId;
@Setter private String ownerId;
```

O scheduler define esses valores quando cria uma tarefa.

---

### Resultado acessível (com @Getter)

```java
@Getter
private Integer statusCode;
```

Permite que o scheduler leia o resultado da tarefa.

---

# 🧩 Construtor

```java
public RequestTasks(WebClient webClient, LogRepository logRepository) {
    this.webClient = webClient;
    this.logRepository = logRepository;
}
```

O Spring injeta o `WebClient` e o `LogRepository`.

---

# ▶️ Método `run()` — Ponto de Entrada da Tarefa

```java
@Override
public void run() {
    if ("TCP".equalsIgnoreCase(this.type)) {
        checkTCP();
    } else {
        checkHTTP();
    }
}
```

- Se o tipo for **TCP**, chama o teste TCP.
- Caso contrário, testa HTTP.

---

# 🌐 Método `checkTCP()`

```java
long start = System.currentTimeMillis();
```
Marca o tempo inicial (para medir duração).

---

### Tentativa de conexão

```java
try (Socket socket = new Socket()) {
    socket.connect(new InetSocketAddress(this.url, this.port), 3000);
    this.statusCode = 200;
    LOG.info("TCP OK: {}:{}", this.url, this.port);
}
```

- Cria um socket vazio.
- Tenta conectar no endereço.
- Timeout: **3000ms (3 segundos)**.
- Se conectar → status **200**.

---

### Em caso de erro

```java
catch (IOException e){
    this.statusCode = 503;
    LOG.warn("TCP FALHA: {}:{} - {}", this.url, this.port, e.getMessage());
}
```

- 503 → Porta inacessível / timeout / erro geral.

---

# 🌎 Método `checkHTTP()`

```java
long start = System.currentTimeMillis();
```

---

### Tentando acessar a URL

```java
this.statusCode = webClient.get()
    .uri(this.url)
    .retrieve()
    .toBodilessEntity()
    .map(response -> response.getStatusCode().value())
    .block();
```

Explicação passo a passo:

| Parte | O que faz |
|------|------------|
| `.get()` | Faz uma requisição HTTP GET |
| `.uri(this.url)` | Endereço da URL monitorada |
| `.retrieve()` | Busca o resultado |
| `.toBodilessEntity()` | Ignora o body, pega só o cabeçalho |
| `.map(...)` | Extrai o número do status HTTP |
| `.block()` | Espera a resposta **sincronamente** |

Se funcionar:  
👉 Loga `"HTTP OK"`  
👉 `statusCode` recebe o código real (200, 301, etc)

---

### Em caso de erro

```java
catch (Exception e) {
    this.statusCode = 500;
    LOG.warn("HTTP FALHA: {} | Erro: {}", this.url, e.getMessage());
}
```

500 → erro ao tentar conectar.

---

### Cálculo do tempo de request

```java
long duration = System.currentTimeMillis() - start;
```

---

### Salvar log (NÃO EXPLICADO — EM CONSTRUÇÃO)

```java
salvarLog(this.statusCode, duration);
```

---

# 📄 Método `salvarLog(int status, long timeMs)`

Este método é responsável pela persistência dos dados de monitoramento no MongoDB.

```java
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

✔️ Conclusão

A classe RequestTasks atua como um "worker" autônomo. Ela é instanciada, executa o teste (TCP ou HTTP), cronometra a duração, salva o resultado no banco e é descartada, garantindo eficiência no uso de memória do Scheduler.


---

### 3. Nova Documentação: `documentation/scheduler.md`

Como você fez alterações profundas no agendamento (limites, mapas de memória, start/stop), o `Scheduler` merece um arquivo próprio. Crie este arquivo.

```markdown
# ⏱️ Documentação: Agendador de Tarefas (`SchedulerService`)

O `SchedulerService` é o coração pulsante do WebBeat. Ele gerencia a execução concorrente de tarefas de monitoramento usando `ThreadPoolTaskScheduler` do Spring.

---

## 1. Arquitetura em Memória

Diferente de um CRUD comum, o Scheduler precisa manter o controle das threads ativas.

### Estruturas de Dados:
* `Map<String, ScheduledFuture<?>> tasks`: Mapeia o ID do serviço (`monitoredId`) para a sua tarefa agendada (`ScheduledFuture`). Isso permite cancelar tarefas específicas individualmente.
* `Map<String, Monitored> apis`: Cache local das configurações dos serviços para acesso rápido pelas threads.

## 2. Controle de Tarefas e Limites

O serviço implementa regras de negócio estritas para controle de recursos.

### Iniciar Monitoramento (`allowMonitoring`)
* **Rota:** Utilizado via `PATCH /task/allow/{id}`.
* **Regra de Negócio (Limite):** Antes de iniciar, verifica se o usuário já possui **5 serviços ativos**. Se exceder, lança exceção e bloqueia o agendamento.
* **Fluxo:**
    1.  Verifica contagem no banco (`countByOwnerId...`).
    2.  Atualiza status no banco para `true`.
    3.  Chama `startSingleTask` para agendar a thread.

### Parar Monitoramento (`removeMonitoring`)
* **Rota:** Utilizado via `PATCH /task/remove/{id}`.
* **Fluxo:**
    1.  Recupera a `ScheduledFuture` do mapa `tasks`.
    2.  Chama `.cancel(true)` para interromper a thread imediatamente.
    3.  Remove a entrada do mapa.
    4.  Atualiza status no banco para `false`.

## 3. Execução de Tarefas (`startSingleTask`)

O método `startSingleTask` é a fábrica que coloca a `RequestTasks` em operação.

1.  **Factory:** Utiliza `ObjectFactory<RequestTasks>` para obter uma **nova instância** (Prototype) da tarefa.
2.  **Configuração:** Injeta URL, Porta, Tipo e IDs na instância da tarefa.
3.  **Agendamento:** Submete a tarefa ao `scheduler.scheduleWithFixedDelay`, usando o intervalo definido pelo usuário (ou padrão de 30s).

## 4. API do Scheduler (`SchedulerController`)

Controla as ações via AJAX do frontend.

| Verbo | Rota | Descrição |
| :--- | :--- | :--- |
| `POST` | `/task/start/{delay}` | Inicializa o scheduler globalmente (boot). |
| `POST` | `/task/stop` | Para todas as tarefas do sistema (Global Kill Switch). |
| `PATCH` | `/task/allow/{id}` | Inicia monitoramento de um serviço específico. Aceita param `delay`. |
| `PATCH` | `/task/remove/{id}` | Para monitoramento de um serviço específico. |
| `GET` | `/task/status/{id}` | Retorna o último código de status HTTP/TCP registrado. |