# Oficina API — Spring Boot

API REST para autenticação e módulos de gestão de oficina mecânica. Requer Java 21, Maven e PostgreSQL.

## Configuração
1. Crie o banco `oficina` no PostgreSQL.
2. Execute os scripts `../banco-de-dados/V1__tabelas.sql` até `V7__views_dashboard.sql`, nessa ordem.
3. Configure `src/main/resources/application.properties` com usuário/senha do banco, chave JWT de pelo menos 32 caracteres e senha local forte para o Administrador Master.
4. Na pasta `backend`, execute `mvn spring-boot:run`.
5. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Autenticação
`POST /auth/login` recebe `{"login":"admin@oficina.local","senha":"SUA_SENHA"}`. Use o token retornado no header `Authorization: Bearer <token>` nos endpoints protegidos.

## Recursos
- `GET/POST /gerente/clientes`, `GET/PUT/DELETE /gerente/clientes/{id}`
- `/gerente/veiculos`, `/gerente/pecas`, `/gerente/estoque`, `/gerente/ordens-servico`, `/gerente/itens-os`, `/gerente/movimentacoes-estoque`, `/gerente/solicitacoes-pecas`, `/gerente/despesas` (mesmo padrão CRUD)
- `/admin/funcionarios` (CRUD exclusivo do Administrador Master)
- `/gerente/dashboard/resumo`, `/gerente/dashboard/estoque-baixo`, `/gerente/dashboard/os-atrasadas`
- `/gerente/clientes/por-cpf/{cpf}`, `/gerente/veiculos/por-placa/{placa}`, `/gerente/clientes/{id}/veiculos`, `/gerente/veiculos/{id}/timeline`
- `/gerente/notificacoes/usuario/{usuarioId}`, `PATCH /gerente/notificacoes/{id}/lida`

Os corpos de criação/atualização usam JSON com nomes de campos em camelCase ou snake_case, conforme as colunas do esquema SQL. Consulte o README da raiz do projeto para exemplos.
