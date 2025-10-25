# 📄 Arquivo `.env` — Configuração do Projeto WebBeat

Este arquivo contém as variáveis de ambiente necessárias para configurar o acesso ao banco de dados MongoDB e a integração com o Spring Boot.

## 🧩 Estrutura do arquivo `.env`

Crie um arquivo chamado **`.env`** na raiz do projeto e adicione o seguinte conteúdo:

```env
# --- Configurações do MongoDB ---
MONGO_ROOT_USER='NomeDoUsuarioRoot'
MONGO_ROOT_PASS='SenhaDoUsuarioRoot'
MONGO_DB_NAME=webbeat

# --- Configurações do Spring Boot ---
SPRING_MONGO_HOST=localhost
SPRING_MONGO_PORT=27017

SPRING_MONGO_USER='${MONGO_ROOT_USER}'
SPRING_MONGO_PASS='${MONGO_ROOT_PASS}'
SPRING_MONGO_AUTH_DB='${MONGO_DB_NAME}'
