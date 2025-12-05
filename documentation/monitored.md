# 📘 Relatório de Implementação: Gestão de URLs (Multi-Tenant)

**Resumo:** Implementação completa do fluxo CRUD para serviços monitorados, com suporte a múltiplos protocolos (HTTP/TCP), intervalos customizáveis e controle de estado em tempo real.

## 1. Arquitetura e Componentes

A solução segue o padrão MVC, expandida para suportar agendamento dinâmico.

* **Domínio (`Monitored.java`):** Entidade imutável (Record) mapeada para a coleção `URLs`.
    * **Novos Campos:**
        * `type`: Define o protocolo (`HTTP` ou `TCP`).
        * `port`: Porta específica (obrigatório para TCP).
        * `interval`: Intervalo de verificação em segundos (padrão: 30s).
        * `beingMonitored`: Flag booleana que indica se o agendador deve processar este serviço.

* **DTO (`MonitoredDTO.java`):** Transfere dados de entrada, incluindo a seleção de protocolo e intervalo.

## 2. Camada de Serviço (`MonitoredService`)

Além do CRUD básico, o serviço agora gerencia o estado de monitoramento.

* **Isolamento de Dados:** Mantém a restrição por `ownerId`.
* **Controle de Estado (`toggleMonitored`):** Permite ativar ou pausar o monitoramento de um serviço específico sem deletá-lo do banco. Atualiza o status `beingMonitored` e registra o timestamp de início.
* **Atualização Atômica:** O método `updateStatusAndInterval` permite ajustar a frequência de monitoramento dinamicamente.

## 3. Camada de Controle (`MonitoredController`)

Atua como orquestrador entre o HTTP, o Serviço de Banco de Dados e o Agendador.

* **Integração com Scheduler:** Ao carregar a lista (`GET /monitored`), o controller invoca `schedulerService.allApis(user.getId())` para garantir que o mapa de tarefas em memória esteja sincronizado com o banco de dados.
* **Rotas Principais:**
    * `GET /monitored`: Lista serviços e sincroniza scheduler.
    * `POST /monitored/add`: Cadastra serviço (HTTP ou TCP).
    * `POST /monitored/update/{id}`: Atualiza configurações.

## 4. Frontend (Thymeleaf + JS)

A interface (`allURLs.html`) evoluiu para um painel de controle interativo.

* **Visualização:** Renderiza badges de status ("Live"/"Stopped") e ícones de protocolo.
* **Interatividade (AJAX/Fetch):**
    * **Toggle Switch:** Checkboxes na tabela disparam requisições `PATCH` para `/task/allow/{id}` ou `/task/remove/{id}`. Isso inicia ou para o monitoramento em tempo real sem recarregar a página.
    * **Calculadora de Intervalo:** Conversor visual de segundos para minutos/horas.
    * **Feedback:** Sistema de "Toasts" (notificações flutuantes) informa sucesso ou erro (ex: limite de 5 serviços atingido).