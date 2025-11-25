📘 Relatório de Implementação: Gestão de URLs (Multi-Tenant)

Resumo: Implementação completa do fluxo CRUD (Create, Read, Update, Delete) para serviços monitorados, garantindo isolamento estrito de dados por usuário (multi-tenancy) e renderização server-side.

1. Arquitetura e Componentes

A solução segue o padrão MVC (Model-View-Controller) com uma camada de serviço intermediária para regras de negócio.

    Domínio (Monitored.java): Entidade imutável (Java Record) mapeada para a coleção URLs no MongoDB. Inclui o campo crucial ownerId para vincular o dado ao usuário.

    DTO (MonitoredDTO.java): Objeto de transferência de dados para desacoplar a entrada do usuário da entidade de banco de dados, prevenindo Over-Posting.

2. Camada de Serviço (MonitoredService)

Responsável pela integridade dos dados e aplicação das regras de negócio.

    Isolamento de Dados: Todos os métodos exigem o ownerId. O método findByOwnerId delega ao repositório a busca filtrada, garantindo que o usuário veja apenas seus registros.

    Prevenção de Duplicidade: Implementada validação existsByOwnerIdAndLink no cadastro para evitar monitoramento redundante da mesma URL pelo mesmo usuário.

    Segurança IDOR: Nos métodos updateMonitored e removeMonitored, o serviço busca o registro pelo ID e imediatamente verifica se o ownerId do documento corresponde ao do usuário logado. Se houver divergência, uma exceção é lançada antes de qualquer modificação.

3. Camada de Controle (MonitoredController)

Atua como orquestrador entre o HTTP e o Serviço.

    Rotas:

        GET /monitored: Lista os serviços.

        POST /monitored/add: Cadastra novo serviço.

        POST /monitored/delete/{id}: Remove serviço.

        POST /monitored/update/{id}: Atualiza serviço.

    Segurança da Sessão: Utilização da anotação @AuthenticationPrincipal CustomUserDetails user em todos os endpoints. Isso extrai o ID do usuário diretamente do contexto de segurança do Spring, tornando impossível a falsificação de identidade via parâmetros da requisição.

4. Frontend (Thymeleaf)

   Visualização (allURLs.html): Renderiza a tabela de serviços iterando sobre a lista enviada pelo Controller. Utiliza formulários ocultos para ações de DELETE (convertendo cliques em requisições POST).

   Edição (editURL.html): Formulário populado com dados existentes, permitindo a alteração transparente de propriedades do serviço.