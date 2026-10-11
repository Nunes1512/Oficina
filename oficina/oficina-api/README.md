# Oficina API

Back-end do sistema de gerenciamento de oficina mecânica.

## Stack
- Java 21
- Spring Boot 3.5.x
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- BCrypt
- Swagger/OpenAPI

## Abrir no NetBeans
1. File > Open Project.
2. Selecione a pasta `oficina-api`.
3. Aguarde o Maven baixar as dependências.
4. Configure `src/main/resources/application.properties`.
5. Execute `OficinaApiApplication`.

## Banco
O projeto usa:
`jdbc:postgresql://localhost:5432/oficina`

O `ddl-auto` está em `validate` para NÃO criar nem alterar tabelas automaticamente.
A entidade `Usuario` espera uma tabela `usuario`. Confirme com o responsável pelo banco antes de executar.

## Senhas
NÃO coloque senha real no GitHub.
`application.properties` está no `.gitignore`.
Mantenha `application-example.properties` apenas com valores de exemplo.

Para criar o Admin Master, configure localmente:
app.admin.login=admin
app.admin.password=SUA_SENHA_LOCAL

## Endpoints iniciais
POST /auth/login
GET /teste
Swagger: /swagger-ui.html

### Login
POST /auth/login
Content-Type: application/json

{
  "login": "admin",
  "senha": "SUA_SENHA"
}

Resposta:
{
  "token": "...",
  "tipo": "Bearer",
  "login": "admin",
  "role": "ADMIN_MASTER"
}

Depois, no Postman:
Authorization > Bearer Token > cole o token.

## Próximas etapas
1. Conferir o schema real do banco do grupo.
2. Ajustar entidades conforme o DER do Cauã.
3. Criar CRUD de usuários.
4. Clientes e veículos.
5. Ordens de serviço.
6. Estoque e solicitações.
7. Dashboard.
8. Timeline e notificações.
9. Testes e documentação.
