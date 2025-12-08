# 📊 Documentação Técnica: Módulo de Relatórios e Dashboard

Este módulo é responsável por transformar os dados de monitoramento (logs) em estatísticas e métricas de desempenho para exibição no Dashboard, mantendo o isolamento estrito de dados por usuário (`ownerId`).

## 1. Data Transfer Objects (DTOs)

Utilizados para estruturar o envio de dados do **Service** para o **Controller**.

### `DashboardStatsDTO.java`
Agrega todas as métricas calculadas pelo sistema em um único objeto para ser enviado à camada de visualização (ou API) do Dashboard.

* **Campos Chave:**
    * `totalUrls`: Número total de serviços monitorados pelo usuário.
    * `servicesOnline`: Número de serviços que estão atualmente (último check) com status OK (200).
    * `uptimePercentage`: Porcentagem de uptime das últimas 24 horas.
    * `totalAlerts`: Total de falhas registradas no sistema.
    * `uptimeHistory`: Lista de pontos de dados para o gráfico de histórico.

### `ChartDataPointDTO.java`
Representa um ponto de dados genérico (rótulo e valor) para uso na construção de gráficos.

* **Campos Chave:**
    * `label`: Rótulo do ponto (ex: dia da semana).
    * `value`: Valor numérico (ex: percentual de uptime).

## 2. Modelos (Domain Entities)

Modelos que representam as coleções de dados no **MongoDB**.

### `LogEntry.java`
Registra o resultado de cada verificação (ping) de URL realizada pelo Scheduler.

* **Coleção:** `logs`
* **Campos Chave:**
    * `ownerId`: ID do usuário proprietário (Garante o multi-tenancy).
    * `monitoredId`: ID do serviço monitorado (URL).
    * `timestamp`: Momento exato da checagem. Possui um índice com `expireAfter = "604800s"` (1 semana) para **remover logs antigos automaticamente**, otimizando o armazenamento (Time-To-Live - TTL).
    * `statusCode`: Código de resposta HTTP (Ex: 200, 404, 500).
    * `responseTime`: Tempo de resposta em milissegundos.

### `DailyStat.java`
Armazena estatísticas consolidadas por dia para construir o histórico de performance.

* **Coleção:** `daily_stats`
* **Campos Chave:**
    * `ownerId`: ID do usuário.
    * `date`: Data da estatística.
    * `uptimePercentage`: Percentual de uptime calculado para aquele dia.

## 3. Repositórios (Spring Data MongoDB)

Interfaces que definem os métodos de acesso aos dados, aproveitando o poder da nomeação de métodos do Spring Data.

### `MonitoredRepository.java` 
Adição de método para contagem de serviços por usuário.

* `long countByOwnerId(String ownerId)`: Conta o número total de documentos `Monitored` pertencentes a um usuário específico.

### `LogRepository.java` 
Responsável por consultas complexas sobre o histórico de checagens.

* `List<LogEntry> findByOwnerIdAndTimestampAfter(String ownerId, Instant timestamp)`: Busca todos os logs de um usuário após um determinado ponto no tempo (ex: últimas 24 horas).
* `long countByOwnerIdAndStatusCodeNot(String ownerId, Integer statusCode)`: Calcula o total de alertas, contando todos os logs onde o status code **não** é 200.
* `Optional<LogEntry> findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc(String ownerId, String monitoredId)`: Encontra o **último log** de um serviço específico para determinar seu status atual (Online/Offline).

### `DailyStatRepository.java` 
Usado para buscar dados consolidados para o histórico de longo prazo (gráfico de uptime).

* `List<DailyStat> findByOwnerIdAndDateAfterOrderByDateAsc(String ownerId, LocalDate date)`: Busca estatísticas diárias de um usuário a partir de uma data específica, ordenadas cronologicamente.

## 4. Camada de Serviço (Business Logic)

### `DashService.java`
O serviço central para o módulo de relatórios, responsável por calcular todas as métricas.

* **Injeção de Dependências:** Recebe `LogRepository`, `MonitoredRepository` e `DailyStatRepository`.
* **Métodos Principais:**
    * `getDashboardStats(String userId)`:
        1.  Obtém o `totalUrls` chamando `monitoredRepository.countByOwnerId()`.
        2.  Calcula o `totalAlerts` chamando `logRepository.countByOwnerIdAndStatusCodeNot()` (status $\neq$ 200).
        3.  Calcula o **Uptime Percentage (24h)**: Busca todos os logs das últimas 24 horas e calcula a proporção de `successChecks` (status == 200) sobre o `totalChecks`.
        4.  Calcula **Services Online**: Itera sobre todos os serviços monitorados do usuário e, para cada um, usa `logRepository.findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc()` para verificar se o último status registrado foi 200.
        5.  Busca o `uptimeHistory` (Histórico de Uptime) usando o método auxiliar.
        6.  Retorna o `DashboardStatsDTO` preenchido.
    * `getAggregatedUptimeHistory(String userId)`:
        1.  Busca as estatísticas diárias (`DailyStat`) da última semana (`oneWeekAgo`) através de `dailyStatRepository.findByOwnerIdAndDateAfterOrderByDateAsc()`.
        2.  Mapeia os resultados para uma lista de `ChartDataPointDTO` para uso no frontend.

## 5. Camada de Controle

### `DashController.java` 
Responsável por extrair o ID do usuário autenticado e usar o serviço para obter as estatísticas antes de renderizar a página.

* **Endpoint:** `GET /dashboard`
* **Segurança:** Utiliza a anotação `@AuthenticationPrincipal CustomUserDetails user` para extrair o `userId` (ID do proprietário) **diretamente do contexto de segurança do Spring**, garantindo que as estatísticas solicitadas sejam apenas as do usuário logado.
* **Fluxo:**
    1.  Chama `dashService.getDashboardStats(user.getId())`.
    2.  Adiciona o `DashboardStatsDTO` (sob o nome `stats`) ao objeto `Model`.
    3.  Retorna o template `dashboard`.

---