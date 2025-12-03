# 🔐 Documentação: Autenticação e Registro (Login/Register)

Este documento detalha o fluxo de autenticação (AuthN) e cadastro de usuários no WebBeat. O sistema utiliza **Spring Security** integrado ao **MongoDB**, com proteção contra força bruta via Rate Limiting em memória.

---

## 1. Arquitetura de Segurança (`SecurityConfig.java`)

A segurança é configurada via `SecurityFilterChain`, operando no modelo **Stateful** (baseado em Sessão/Cookies).

### Configurações Principais:
* **Rotas Públicas:** `/auth/**` (Login, Registro, Recuperação de Senha) e recursos estáticos (`/css`, `/js`).
* **Rotas Protegidas:** Todas as demais requisições exigem autenticação (`.anyRequest().authenticated()`).
* **Criptografia:** Utiliza `BCryptPasswordEncoder` para hashing de senhas.
* **Login Form:** Página customizada em `/auth/login`.
* **Logout:** Invalida a sessão HTTP e redireciona para o login.

---

## 2. Registro de Usuários (`Register`)

O processo de criação de conta converte um DTO inseguro em uma Entidade persistente protegida.

**Localização:** `src/main/java/com/webbeat/webbeat/service/UserService.java`

### Fluxo de Dados:
1.  **Entrada:** O `AuthController` recebe um `UserDTO` (email, senha bruta).
2.  **Validação:** O `UserService` verifica se o email já existe no `UserRepository`.
3.  **Hashing:** A senha bruta é processada pelo `passwordEncoder` (BCrypt + Salt).
4.  **Persistência:** Um novo objeto `User` (