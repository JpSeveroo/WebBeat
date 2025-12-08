# 💻 WebBeat  
## Monitoramento Inteligente de Disponibilidade e Performance

🌐 **Clique aqui para acessar o WebBeat Online**

O **WebBeat** é uma plataforma **SaaS open-source** projetada para monitorar, em tempo real, a saúde de **sites**, **APIs** e **serviços TCP**.  
Ele funciona como uma alternativa totalmente gratuita e extensível a ferramentas comerciais — garantindo que **você seja o primeiro a saber** quando sua infraestrutura apresentar instabilidade.

---

# 🚀 Funcionalidades Principais

## 📡 Monitoramento Multi-Protocolo

### **HTTP/HTTPS**
- Verifica o **status code** (ex.: 200 OK).  
- Mede o **tempo de resposta** de sites e APIs REST.  
- Identifica falhas como timeouts, DNS e erros 4xx/5xx.

### **TCP / Port Check**
- Testa conectividade em portas específicas.  
- Ideal para **bancos de dados**, **servidores de jogos**, **SMTP**, etc.

---

## 🔔 Sistema de Alertas em Tempo Real

### **Integração com Telegram**
- Notificações instantâneas direto no seu celular quando um serviço cair.

### **Detecção de Erros**
Identifica automaticamente:
- Timeout  
- Problemas de DNS  
- Falhas de conexão  
- Erros HTTP (4xx e 5xx)

---

## 📊 Dashboard Analítico

### **Visão Geral**
- Métricas de **Uptime (24h)**  
- Quantidade total de serviços monitorados  
- Alertas recentes

### **Gráficos (Chart.js)**
- Histórico de latência  
- Histórico de disponibilidade  

### **Gestão de Intervalos**
- Cada serviço pode ter sua própria frequência de checagem  
- Intervalos variam de **segundos a horas**

---

## 🛡️ Segurança e Gestão de Usuários

- **Autenticação Segura:** Login/Registro com BCrypt  
- **Recuperação de Senha:** Token via e-mail (SMTP)  
- **Multi-Tenant:** Cada usuário vê **somente seus próprios serviços e logs**  

---

# 📸 Screenshots (Descrição)

### Dashboard
- Visão geral de métricas, uptime e gráficos.

### Gerenciamento de URLs
- CRUD completo de serviços monitorados.

---

# 🛠️ Stack Tecnológica

O WebBeat foi construído com **Clean Architecture** e princípios **SOLID**, utilizando tecnologias modernas.

## Backend
- **Java 17 LTS**  
- **Spring Boot** (Web, Security, Mail, Validation)  
- **Spring Data MongoDB**  
- **Spring Scheduler:** tarefas concorrentes com `ThreadPoolTaskScheduler`  
- **WebClient (Reactive):** HTTP não-bloqueante para alta performance  

## Frontend
- **Thymeleaf (SSR)**  
- **TailwindCSS**  
- **Chart.js**

## Infraestrutura & DevOps
- **Docker & Docker Compose**  
- **Telegram Bot API** (gateway de alertas)

---

# 🧠 Arquitetura do Sistema

O sistema opera em um fluxo contínuo de verificação:

### **1. Scheduler Service**
- Gerencia um pool de threads.  
- Cada serviço monitorado é executado como uma task separada (**RequestTask**).

### **2. Worker (RequestTask)**
Executa o "ping" no serviço alvo:  
- **Sucesso:**  
  - Registra tempo de resposta e HTTP 200 no MongoDB  
- **Falha:**  
  - Registra o erro  
  - Aciona o **TelegramService** para notificação

### **3. Alert System**
- `TelegramIntegration` usa programação **reativa**  
- Evita bloquear threads de monitoramento

### **4. Data Persistence**
- Logs armazenados na coleção **logs** do MongoDB  
- Utilizados para relatórios e gráficos de uptime

---

# 📜 Licença
Este projeto está sob a **MIT License**.  
Consulte o arquivo `LICENSE` para mais detalhes.

---

<p align="center">
Desenvolvido por <strong>estudantes de Engenharia de Software da UPE 🐍</strong>
</p>
