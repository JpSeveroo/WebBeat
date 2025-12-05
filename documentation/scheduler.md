# Documentação do Módulo Scheduler (WebBeat)

Este módulo é responsável pelo agendamento e execução de tarefas de monitoramento de APIs em background. Ele utiliza o `ThreadPoolTaskScheduler` do Spring Framework para gerenciar a execução concorrente de verificações de disponibilidade (health checks) das APIs cadastradas pelos usuários.

## ⚙️ Arquitetura

O sistema é composto por três componentes principais que orquestram o fluxo de monitoramento:

### 1. Configuração (`SchedulerConfig`)
Define a infraestrutura de threads para o agendamento.
* **Pool Size**: 5 threads (configurável).
* **Thread Prefix**: `API-monitoring-`.
* **Política de Encerramento**: Aguarda a conclusão das tarefas em execução por até 30 segundos antes de encerrar forçadamente (`setWaitForTasksToCompleteOnShutdown`).

### 2. Controlador (`SchedulerController`)
Expõe endpoints REST para interação com o frontend ou sistemas externos. Gerencia o ciclo de vida do monitoramento (iniciar, parar, adicionar/remover tarefas específicas).
* **Base URL**: `/task`

### 3. Serviço (`SchedulerService`)
Contém a lógica de negócio e o gerenciamento de estado em memória.
* **Gerenciamento de Estado**: Utiliza `ConcurrentHashMap` para manter referências thread-safe das tarefas agendadas (`ScheduledFuture<?>`) e dos metadados das APIs (`Monitored`).
* **Limitação de Recursos**: Impõe um hard limit de 5 serviços monitorados simultaneamente por usuário para evitar exaustão de recursos.
* **Persistência**: Interage com `LogRepository` e `MonitoredRepository` para buscar configurações e salvar logs de execução.

---

## 🔌 API Reference

### Iniciar Monitoramento Global
Inicia o agendamento para todas as APIs cadastradas do usuário autenticado.

`POST /task/start/{delay}`

| Parâmetro | Tipo      | Descrição |
| :--- | :--- | :--- |
| `delay`   | `Integer` | Intervalo padrão em segundos entre as execuções (caso a API não tenha um intervalo específico). |

### Parar Monitoramento Global
Cancela todas as tarefas de monitoramento ativas no sistema e limpa o mapa de tarefas em memória.

`POST /task/stop`

### Ativar Monitoramento Individual
Habilita o monitoramento para uma API específica, respeitando o limite de 5 tarefas ativas.

`PATCH /task/allow/{id}?delay={segundos}`

| Parâmetro | Tipo      | Descrição |
| :--- | :--- | :--- |
| `id`      | `String`  | ID do serviço monitorado (MongoDB ID). |
| `delay`   | `Integer` | (Opcional) Intervalo em segundos. Default: 30. |

**Retorno de Erro:**
* `400 Bad Request` ou `500 Internal Server Error` (via `IllegalStateException`) se o limite de 5 serviços for excedido.

### Remover Monitoramento Individual
Desativa o monitoramento de uma API específica e cancela sua tarefa agendada imediatamente.

`PATCH /task/remove/{id}`

| Parâmetro | Tipo     | Descrição |
| :--- | :--- | :--- |
| `id`      | `String` | ID do serviço a ser removido. |

### Consultar Status Recente
Retorna o último código de status HTTP registrado para o serviço.

`GET /task/status/{id}`

---

## 🧠 Detalhes de Implementação

### Controle de Concorrência
O serviço mantém um mapa `tasks` (`Map<String, ScheduledFuture<?>>`) para rastrear os "handlers" das tarefas agendadas. Isso permite o cancelamento cirúrgico de threads específicas sem afetar o restante do pool.

### Fluxo de Execução (`startSingleTask`)
1.  Verifica se a tarefa já existe e está ativa; se sim, cancela a anterior para evitar duplicidade.
2.  Instancia um novo `RequestTasks` via `ObjectFactory` (garantindo escopo de protótipo se configurado).
3.  Agenda a execução usando `scheduler.scheduleWithFixedDelay`.
4.  Armazena o `ScheduledFuture` resultante no mapa de controle.

### Tratamento de Limites
O método `allowMonitoring` realiza uma contagem prévia no banco de dados (`monitoredRepository`) filtrando por `beingMonitored: true`. Se `count >= 5`, uma exceção é lançada antes de qualquer alteração de estado."