# Oficina API — Backend integrado ao PostgreSQL

Requisitos: **Java 21**, **Maven** e **PostgreSQL 18** (ou versão compatível).

## 1. Preparar o banco

No pgAdmin, conecte ao servidor PostgreSQL e crie um banco chamado `oficina`:

```sql
CREATE DATABASE oficina;
```

Não execute os arquivos SQL manualmente. O Flyway está configurado para executar automaticamente as migrations `V1` até `V7`, que ficam em `src/main/resources/db/migration`, na primeira inicialização da API.

## 2. Configurar o acesso

Abra `src/main/resources/application.properties` e informe a senha do PostgreSQL em `spring.datasource.password` (ou defina a variável de ambiente `DB_PASSWORD`).

Também configure:
- `app.jwt.secret`: chave local aleatória com pelo menos 32 caracteres (ou `JWT_SECRET`);
- `app.admin.password`: senha forte para o primeiro Administrador Master (ou `ADMIN_PASSWORD`).

Não publique senhas nem chaves no Git.

## 3. Iniciar a API

No terminal, dentro da pasta `backend`:

```bash
mvn spring-boot:run
```

Na primeira execução, o Flyway cria as tabelas, índices, perfis, triggers e views. O sistema cria o usuário Admin Master com o login configurado, desde que a senha não esteja no valor de exemplo `ALTERE_AQUI`.

## 4. Testar

- Swagger: http://localhost:8080/swagger-ui.html
- Teste simples: http://localhost:8080/teste
- Login: `POST http://localhost:8080/auth/login`

Exemplo de corpo do login:

```json
{
  "login": "admin@oficina.local",
  "senha": "SUA_SENHA_CONFIGURADA"
}
```

Use o token retornado no cabeçalho `Authorization: Bearer SEU_TOKEN` nos endpoints protegidos.

## Atenção se o banco já tiver tabelas

O Flyway espera um banco novo/vazio. Se você já executou os scripts V1 a V7 manualmente no banco `oficina`, não inicie a aplicação com Flyway habilitado nesse mesmo banco sem antes planejar a baseline/migração; caso contrário, a primeira migration poderá falhar porque as tabelas já existem. Faça backup antes de qualquer alteração.
