# Documentação: Módulo de Notificação do Telegram (WebBeat)

Este documento descreve a arquitetura do sistema de notificações do Telegram. O objetivo é enviar mensagens para um chat específico através de um Bot.

O fluxo de dados segue a separação padrão de camadas: `Controller` $\rightarrow$ `Service` $\rightarrow$ `Integration`.

## Componentes do Sistema

Abaixo está o detalhamento de cada arquivo criado para esta funcionalidade:

### 1. `webbeat/config/TelegramConfig.java`

- **Responsabilidade:** Configuração do Cliente HTTP.
    
- **O que faz:** Esta classe usa a anotação `@Configuration` do Spring para definir um `Bean`. Esse `Bean` cria e configura uma instância do `WebClient`, que é a ferramenta usada para fazer requisições HTTP. Ela define a URL base da API do Telegram (`https://api.telegram.org`), para que as outras classes não precisem repetir essa URL.
    

### 2. `webbeat/integration/TelegramIntegration.java`

- **Responsabilidade:** Camada de Comunicação com a API.
    
- **O que faz:** Esta é a classe que "fala" diretamente com o Telegram.
    
    - Recebe o `WebClient` configurado no passo anterior.
        
    - Usa a anotação `@Value` para injetar com segurança o Token do Bot e o ID do Chat a partir das variáveis de ambiente (arquivo `.env`).
        
    - Possui o método `enviarMensagem` que constrói a requisição GET final. Ele monta o _path_ (ex: `/bot<TOKEN>/sendMessage`) e adiciona os parâmetros necessários (`chat_id` e `text`).
        

### 3. `webbeat/service/TelegramService.java`

- **Responsabilidade:** Camada de Lógica de Negócio.
    
- **O que faz:** Serve como uma ponte entre quem _pede_ o envio (o Controller) e quem _executa_ (a Integration).
    
    - Recebe a classe `TelegramIntegration`.
        
    - O método `envioMensagem` chama a camada de integração para disparar a mensagem.
        
    - Ele também é responsável por "ativar" a requisição reativa (usando `.subscribe()`) e logar uma mensagem de sucesso no console.
        

### 4. `webbeat/controller/TelegramTestController.java`

- **Responsabilidade:** Ponto de Entrada para Testes.
    
- **O que faz:** Expõe um _endpoint_ HTTP (uma URL) para que você possa facilmente testar todo o fluxo.
    
    - Usa a anotação `@RestController`.
        
    - Mapeia a URL `/test-telegram` para o método `testTelegram`.
        
    - Quando essa URL é acessada, ela chama o `TelegramService` para enviar uma mensagem de teste pré-definida.
        

### 5. Arquivo `.env`

- **Responsabilidade:** Gerenciamento de Credenciais.
    
- **O que faz:** Este arquivo armazena dados sensíveis (como `TELEGRAM_BOT_TOKEN` e `TELEGRAM_CHAT`) fora do código-fonte. A dependência `spring-dotenv` lê este arquivo e permite que o Spring injete esses valores nas classes (como na `TelegramIntegration`) de forma segura, sem expor as credenciais no código.