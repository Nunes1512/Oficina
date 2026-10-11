# Banco de Dados

Banco de dados do **Sistema de Gerenciamento para Oficina Mecânica**, feito em **PostgreSQL 18**.

Responsável: **Cauã**

## Estrutura

Os scripts são numerados e devem ser executados **em ordem**, porque cada um depende do anterior.

| Arquivo | Conteúdo |
|---|---|
| `V1__tabelas.sql` | Criação das 14 tabelas, chaves e restrições |
| `V2__indices.sql` | Índices para acelerar joins, filtros e consultas por período |
| `V3__dados_iniciais.sql` | Funções (perfis) de acesso: `ROLE_ADMIN_MASTER`, `ROLE_GERENTE`, `ROLE_FUNCIONARIO` |
| `V4__triggers_os_estoque.sql` | Cálculo do total da OS e baixa automática de estoque |
| `V5__triggers_notificacoes.sql` | Notificações de estoque baixo e de solicitação de peça |
| `V6__triggers_timeline_conclusao.sql` | Timeline do veículo e conclusão da OS |
| `V7__views_dashboard.sql` | Views e função que alimentam o dashboard |

## Tabelas

| Tabela | Para que serve |
|---|---|
| `funcao` | Perfis de acesso usados pelo Spring Security |
| `usuario` | Credenciais de login (senha em hash BCrypt) |
| `cliente` | Proprietários dos veículos |
| `funcionario` | Equipe da oficina |
| `veiculo` | Veículos atendidos e seu status |
| `peca` | Catálogo e saldo do estoque |
| `ordem_servico` | Módulo principal do sistema |
| `item_os` | Peças usadas em cada OS (relação N:N) |
| `movimentacao_estoque` | Histórico de entradas e saídas |
| `solicitacao_peca` | Pedidos de peças feitos pelos funcionários |
| `notificacao` | Alertas para os usuários |
| `timeline_veiculo` | Histórico cronológico de cada veículo |
| `auditoria` | Registro de ações críticas |
| `despesa` | Benefícios e outras despesas |

## Relacionamentos principais

```
funcao 1──* usuario
usuario 1──0..1 funcionario
cliente 1──* veiculo
veiculo 1──* ordem_servico
veiculo 1──* timeline_veiculo
funcionario 1──* ordem_servico
ordem_servico *──* peca   (via item_os)
funcionario 1──* solicitacao_peca
peca 1──* solicitacao_peca
peca 1──* movimentacao_estoque
usuario 1──* notificacao
```

## Automações (triggers)

- **Total da OS:** `valor_total` = mão de obra + peças.
- **Baixa de estoque:** ao adicionar uma peça na OS, valida o saldo, desconta do estoque e registra a saída. Sem saldo, a operação é bloqueada.
- **Estoque baixo:** notifica gerentes e admins quando o saldo cruza o mínimo.
- **Solicitação de peça:** notifica a gestão a cada nova solicitação.
- **Timeline:** registra a entrada do veículo e cada mudança de status.
- **Conclusão:** preenche as datas de entrega e de conclusão e notifica a gestão.

## Consultas do dashboard

| Objeto | Uso |
|---|---|
| `vw_dashboard_resumo` | Cards do mês (faturamento, gastos, lucro, veículos, ordens) |
| `vw_dashboard_pizza` | Dados do gráfico de pizza |
| `fn_dashboard_movimentacao(periodo, inicio, fim)` | Gráfico de linha; `periodo` aceita `'day'`, `'month'` ou `'year'` |
| `vw_os_atrasadas` | OS vencidas, usadas para gerar o alerta de atraso |
| `vw_estoque_baixo` | Peças no estoque mínimo ou abaixo dele |

Exemplo:

```sql
SELECT * FROM fn_dashboard_movimentacao('day', current_date - 30, current_date);
```

## Como executar

1. Instale o PostgreSQL e o pgAdmin.
2. Crie o banco:
   ```sql
   CREATE DATABASE oficina;
   ```
3. Execute os arquivos **de V1 a V7, nessa ordem**.

Pelo pgAdmin: clique com o botão direito no banco `oficina`, abra o **Query Tool**, carregue o arquivo e aperte **F5**.

Pelo terminal:

```bash
psql -U postgres -d oficina -f V1__tabelas.sql
psql -U postgres -d oficina -f V2__indices.sql
psql -U postgres -d oficina -f V3__dados_iniciais.sql
psql -U postgres -d oficina -f V4__triggers_os_estoque.sql
psql -U postgres -d oficina -f V5__triggers_notificacoes.sql
psql -U postgres -d oficina -f V6__triggers_timeline_conclusao.sql
psql -U postgres -d oficina -f V7__views_dashboard.sql
```

Para recomeçar do zero, apague o banco e crie de novo. Rodar os scripts duas vezes gera erro de "já existe".

## Verificação

```sql
SELECT * FROM funcao;                -- deve retornar 3 linhas
SELECT * FROM vw_dashboard_resumo;   -- deve retornar 1 linha
```

## Segurança

- A senha do usuário é guardada apenas como **hash BCrypt**.
- O **Admin Master** deve ser criado pela aplicação, nunca por script SQL, porque a senha precisa do hash gerado pelo Spring Security.
- A aplicação deve conectar com um usuário próprio, e não com o `postgres`.
- Senhas e chaves ficam no arquivo `.env`, que **nunca** é commitado.
- Exclusão de usuário é lógica (`ativo = false`), para preservar a auditoria.

## Observações para o back-end

- A tabela de perfis se chama `funcao`, e a coluna em `usuario` é `funcao_id`.
- Os nomes dos perfis mantêm o prefixo `ROLE_`, exigido pelo Spring Security.
- Os triggers já fazem a baixa de estoque, a timeline e as notificações. Se o Service em Java fizer o mesmo, **remova o trigger equivalente** para não duplicar.
- Configure `spring.jpa.hibernate.ddl-auto=validate` para o Hibernate não alterar o esquema.
