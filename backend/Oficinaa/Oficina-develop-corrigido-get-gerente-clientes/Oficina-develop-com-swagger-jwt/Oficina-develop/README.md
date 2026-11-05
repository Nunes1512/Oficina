# Sistema de Gerenciamento de Oficina Mecânica

Projeto com backend Spring Boot, PostgreSQL e frontend.

## Estrutura

- `backend/`: API Java Spring Boot.
- `backend/src/main/resources/db/migration/`: scripts SQL V1 a V7, executados pelo Flyway.
- `frontend/`: interface do sistema.
- `docs/`: documentação.

## Inicialização rápida

1. Instale Java 21, Maven e PostgreSQL.
2. Crie um banco vazio chamado `oficina`.
3. Configure a senha do PostgreSQL e as chaves no `backend/src/main/resources/application.properties`.
4. Abra um terminal na pasta `backend` e execute `mvn spring-boot:run`.
5. Acesse `http://localhost:8080/swagger-ui.html`.

Consulte `backend/README.md` para instruções completas.

**Importante:** se os scripts SQL já foram executados manualmente no banco, leia o aviso sobre Flyway no README do backend antes de iniciar.
