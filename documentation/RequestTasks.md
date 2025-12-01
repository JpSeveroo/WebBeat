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

# 📄 Método `salvarLog()` (NÃO SERÁ EXPLICADO)

> ⚠️ **Este método está em construção** e **não será explicado neste documento**, conforme solicitado.

---

# ✔️ Fim do documento

Este guia explicou **cada parte da classe RequestTasks** de forma detalhada e organizada em Markdown.

Se quiser, posso:

✅ Gerar versão em PDF  
✅ Criar um diagrama UML  
✅ Criar documentação para outra classe  
✅ Explicar o fluxo completo do Scheduler do WebBeat

Só pedir.
