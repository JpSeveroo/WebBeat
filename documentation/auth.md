### 🔑 Mecanismo de Autenticação do WebBeat (Spring Security/Session)

Seu sistema de autenticação opera em um modelo Server-Side com **Spring Security**, utilizando **sessões HTTP (JSESSIONID)** para manter o estado de autenticação após o login. O isolamento de dados (multi-tenant) é garantido pela busca do `userId` no contexto de segurança para filtrar os dados do MongoDB.

| Camada | Componente | Função Primária |
| :--- | :--- | :--- |
| **Configuração** | `SecurityConfig.java` | Define as regras de acesso e o fluxo de formulário. |
| **Persistência** | `User.java` / `UserRepository.java` | Mapeia a coleção `users` do MongoDB e permite buscar usuários pelo `email`. |
| **Integração SS** | `CustomUserDetailsService.java` | Ponte entre o Spring Security e o MongoDB. |
| **Lógica** | `UserService.java` | Gerencia a criação de novos usuários e hash de senhas. |

---

## 🔒 Documentação Técnica de Auth

### 1. Configuração de Segurança (`SecurityConfig.java`)

A classe `SecurityConfig` atua como o **porteiro digital** da aplicação.

* **Acesso Público:** Permite acesso irrestrito aos recursos estáticos (`/css/**`, `/js/**`, etc.) e a todos os endpoints sob `/auth/**` (login, registro).
* **Acesso Protegido:** Qualquer outra requisição (`.anyRequest()`) exige que o usuário esteja **autenticado** (`.authenticated()`).
* **Form Login:** Configura o formulário de login para usar o caminho `/auth/login` e redirecionar para `/dashboard` após o sucesso.
* **Logout:** Define `/logout` como a URL de saída, redirecionando para `/auth/logout` (que é apenas um redirect, pois a remoção da sessão é tratada internamente pelo Spring Security).
* **Encoder de Senha:** Utiliza o robusto `BCryptPasswordEncoder` para aplicar hash nas senhas, garantindo que nunca sejam armazenadas em texto simples no MongoDB.

### 2. Fluxo de Registro (`UserService.java` & `AuthController.java`)

O fluxo de registro é manual e controlado pela camada de serviço:

1.  **Requisição:** O usuário submete o formulário em `/auth/register` (POST).
2.  **Validação:** O `UserService` verifica se o `email` já está em uso no `UserRepository`.
3.  **Hashing:** Se o email for novo, a senha é processada pelo `passwordEncoder` (BCrypt).
4.  **Persistência:** O novo registro `User` (com `passwordHash`) é salvo na coleção `users` do MongoDB.
5.  **Redirecionamento:** Após o sucesso, o usuário é enviado para a tela de login.

### 3. Fluxo de Login (Spring Security Core)

O Spring Security assume a responsabilidade da autenticação via `POST` em `/auth/login`:

1.  **Busca do Usuário:** O `CustomUserDetailsService` recebe o `username` (email) e consulta o `UserRepository` no MongoDB. Se o usuário não for encontrado, lança `UsernameNotFoundException`.
2.  **Contrato `UserDetails`:** O registro `User` do MongoDB é encapsulado na classe `CustomUserDetails` para satisfazer o contrato `UserDetails` exigido pelo Spring Security.
3.  **Verificação de Credenciais:** O Spring Security compara o `passwordHash` recuperado do `CustomUserDetails` com a senha bruta fornecida no formulário (usando o `BCryptPasswordEncoder`).
4.  **Criação da Sessão:** Se as credenciais estiverem corretas, o Spring Security injeta o objeto `Authentication` no `SecurityContextHolder`, cria uma **sessão HTTP (via cookie JSESSIONID)** e redireciona o usuário para o `/dashboard`.

A partir deste ponto, o usuário é considerado autenticado, e o **`userId`** pode ser extraído em qualquer Controller (por exemplo, usando `@AuthenticationPrincipal`) para aplicar o filtro **multi-tenant** e garantir o isolamento de dados.