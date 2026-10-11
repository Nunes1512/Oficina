# Sistema de Oficina — projeto integrado

Este pacote reúne o Back-end Java (Spring Boot) e os scripts SQL PostgreSQL do projeto da oficina.

## Estrutura
- `oficina-api/`: API Java, autenticação JWT e Spring Security.
- `database/`: scripts SQL do banco, que devem ser executados de V1 até V7.

## Importante
A integração inicial foi ajustada para que a entidade `Usuario` use as colunas reais do banco (`email`, `senha`, `ativo`, `bloqueado`, `funcao_id`) e os perfis da tabela `funcao`. Este ainda não é o sistema completo: os CRUDs de clientes, veículos, ordens de serviço, estoque e dashboard em endpoints Java ainda precisam ser desenvolvidos.

## 1. Criar o banco
No PostgreSQL/pgAdmin, crie um banco chamado `oficina`. Depois execute, nessa ordem, os arquivos `database/V1__tabelas.sql` até `database/V7__views_dashboard.sql`. Não execute duas vezes no mesmo banco já preenchido.

## 2. Configurar a API
Edite `oficina-api/src/main/resources/application.properties` e informe usuário/senha do PostgreSQL, uma chave JWT com pelo menos 32 caracteres e os dados locais do Admin Master. Não envie senhas reais ao GitHub.

## 3. Executar
É necessário Java 21 e Maven. No terminal, entre em `oficina-api` e execute `mvn spring-boot:run`.

## Login
`POST http://localhost:8080/auth/login`
```json
{ "login": "admin@oficina.local", "senha": "SUA_SENHA_LOCAL" }
```
A propriedade `app.admin.login` é o e-mail inicial do administrador. A API responde com um JWT Bearer.

Endpoints iniciais: `POST /auth/login`, `GET /teste`, Swagger em `/swagger-ui.html`.

## Módulos Java adicionados

O `oficina-api` agora expõe endpoints REST para os módulos operacionais usando as tabelas SQL existentes. O login continua em `POST /auth/login`; para endpoints protegidos, envie `Authorization: Bearer <token>`.

### Endpoints (após autenticação)

- Clientes: `/gerente/clientes` (CRUD), `/gerente/clientes/pesquisar?q=ana`, `/gerente/clientes/por-cpf/{cpf}` e `/gerente/clientes/{id}/veiculos`.
- Veículos: `/gerente/veiculos` (CRUD), `/gerente/veiculos/por-placa/{placa}` e `/gerente/veiculos/{id}/timeline`.
- Peças/estoque: `/gerente/pecas` ou `/gerente/estoque` (CRUD).
- Ordens de serviço: `/gerente/ordens-servico` (CRUD).
- Itens de OS: `/gerente/itens-os` (CRUD; as triggers SQL existentes calculam valores e movimentam estoque).
- Movimentações: `/gerente/movimentacoes-estoque`.
- Solicitações de peças: `/gerente/solicitacoes-pecas`.
- Despesas: `/gerente/despesas`.
- Funcionários: `/admin/funcionarios` (somente Administrador Master).
- Dashboard: `/gerente/dashboard/resumo`, `/gerente/dashboard/estoque-baixo` e `/gerente/dashboard/os-atrasadas`.
- Notificações: `/gerente/notificacoes/usuario/{usuarioId}` e `PATCH /gerente/notificacoes/{id}/lida`.

### Exemplo de cadastro de cliente

`POST /gerente/clientes` com JSON:

```json
{
  "nome": "Maria da Silva",
  "cpf": "123.456.789-00",
  "telefone": "11999999999",
  "email": "maria@example.com",
  "endereco": "Rua das Flores, 10"
}
```

O JSON aceita campos em camelCase ou snake_case. Para atualizar, use `PUT /gerente/clientes/{id}` com os campos que deseja alterar. As datas devem ser enviadas em formato ISO, por exemplo `2026-10-15T14:30:00`.

### Observações importantes

- Execute `V1` até `V7` no PostgreSQL antes de iniciar a API e configure as credenciais em `oficina-api/src/main/resources/application.properties`.
- O usuário precisa de token e perfil autorizado para acessar os endpoints. Gerenciamento de funcionários foi restringido ao Administrador Master.
- Os endpoints são uma base funcional de CRUD ligada ao esquema atual. A API responde com erros HTTP quando um campo não é permitido, um registro não existe ou uma regra de integridade do PostgreSQL é violada.
- Exclusões são físicas e podem ser recusadas pelo PostgreSQL quando existirem registros relacionados. Para produção, considere exclusão lógica, auditoria completa, DTOs com validação por campo e testes automatizados.
- Não foi possível executar `mvn test` neste ambiente porque o Maven não está instalado. É necessário compilar e testar localmente com Java 21 e Maven antes de considerar o projeto validado.
