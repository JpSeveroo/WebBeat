# 📧 Documentação: Recuperação de Senha (Forgot Password)

O recurso de recuperação de senha implementa um fluxo seguro baseado em **Tokens Temporários (Time-Limited Tokens)**, permitindo que usuários redefinam suas credenciais sem intervenção administrativa. O sistema prioriza a segurança contra enumeração de usuários e abuso de recursos (Rate Limiting).

---

## 1. Arquitetura do Fluxo

O processo é dividido em duas etapas distintas: **Solicitação** e **Redefinição**.

### A. Fluxo de Solicitação (Request)
**Endpoint:** `POST /auth/forgot-password`

1.  **Rate Limiting (Proteção de Cota):** Antes de processar, o `UserService` verifica se o e-mail solicitante já disparou um reset nas últimas **24 horas**. Se sim, a execução é abortada silenciosamente para preservar a cota do servidor SMTP.
2.  **Busca de Usuário:** O sistema busca o `User` pelo e-mail.
    * *Security Note:* Se o usuário **não** existir, o sistema **não** retorna erro. Ele simula um sucesso para impedir que atacantes descubram quais e-mails estão cadastrados na base (Prevenção de *User Enumeration*).
3.  **Geração de Token:**
    * Cria-se um **UUID** aleatório.
    * Define-se a validade para **15 minutos**.
    * O token é salvo na coleção `password_reset_tokens` com referência ao `userId`.
4.  **Disparo de E-mail:** Um e-mail **HTML** estilizado é enviado contendo um link único (`/auth/reset-password?token=UUID`).

### B. Fluxo de Redefinição (Reset)
**Endpoint:** `POST /auth/reset-password`

1.  **Validação do Token:** O sistema busca o token no banco.
    * Se não existir ou se a data de expiração (`expireDate`) já tiver passado, o processo é bloqueado.
2.  **Alteração de Credencial:**
    * O usuário associado ao token é recuperado.
    * A nova senha é criptografada com **BCrypt**.
    * O registro do usuário é atualizado no MongoDB.
3.  **Queima do Token (Token Burning):** Imediatamente após o sucesso, o token é deletado do banco de dados para impedir ataques de repetição (*Replay Attacks*).

---

## 2. Modelo de Dados (`PasswordResetToken`)

Utilizamos uma coleção separada para tokens para manter a coleção de usuários limpa e permitir o uso de índices TTL (Time-To-Live).

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `token` | String (UUID) | O segredo enviado por e-mail (chave de busca). |
| `userId` | String (Ref) | ID do usuário que solicitou o reset. |
| `expireDate` | Instant | Data/Hora limite para uso. |
| **Index** | TTL | Configurado via `@Indexed(expireAfter = "0s")` para auto-remoção (limpeza automática do banco). |

---

## 3. Medidas de Segurança Implementadas

### 🛡️ Rate Limiting "In-Memory"
Para proteger a cota gratuita do serviço de e-mail (Gmail SMTP), implementamos um limitador na camada de serviço:
* **Mecanismo:** `ConcurrentHashMap<Email, Timestamp>`.
* **Regra:** Bloqueia novos envios para o mesmo e-mail por um período de **24 horas**.
* **Comportamento:** Falha silenciosa (o usuário vê a mensagem de sucesso, mas o e-mail não é enviado), desestimulando tentativas de spam.

### 🛡️ Postura Defensiva (Security through Obscurity)
As mensagens de feedback no Controller (`AuthController`) são deliberadamente vagas:
> *"If an account exists for [email], you will receive a reset link shortly."*

Isso impede que bots validem listas de e-mails vazados contra sua aplicação.

---

## 4. Integração de E-mail (JavaMailSender)

O envio utiliza `MimeMessage` para suportar conteúdo rico (HTML/CSS).

* **Template:** O HTML é construído internamente no `UserService` usando *Java Text Blocks*.
* **Estilo:** Utiliza CSS *inline* para garantir compatibilidade com clientes de e-mail (Gmail, Outlook) que removem folhas de estilo externas.
* **Segurança do Link:** O link contém apenas o UUID, sem expor dados sensíveis do usuário na URL.