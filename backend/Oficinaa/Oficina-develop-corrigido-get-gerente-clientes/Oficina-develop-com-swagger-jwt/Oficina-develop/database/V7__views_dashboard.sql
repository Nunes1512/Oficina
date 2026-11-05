-- Lista as OS vencidas; o scheduler do Spring usa para gerar o alerta OS_ATRASADA
CREATE OR REPLACE VIEW vw_os_atrasadas AS
SELECT os.id, os.numero, v.placa, v.previsao_entrega, os.status
  FROM ordem_servico os
  JOIN veiculo v ON v.id = os.veiculo_id
 WHERE os.status NOT IN ('CONCLUIDA','CANCELADA')
   AND v.previsao_entrega < now();

-- Lista as peças que atingiram o estoque mínimo
CREATE OR REPLACE VIEW vw_estoque_baixo AS
SELECT id, nome, fabricante, quantidade, estoque_minimo
  FROM peca
 WHERE quantidade <= estoque_minimo;

-- Cards do dashboard do gerente, calculados para o mês corrente
CREATE OR REPLACE VIEW vw_dashboard_resumo AS
WITH mes AS (SELECT date_trunc('month', now()) AS ini),
fat AS (
    SELECT COALESCE(SUM(valor_total),0)    AS faturamento,
           COALESCE(SUM(valor_mao_obra),0) AS receita_servicos,
           COALESCE(SUM(valor_pecas),0)    AS receita_pecas
      FROM ordem_servico, mes
     WHERE status = 'CONCLUIDA' AND data_conclusao >= mes.ini
),
gp AS (
    SELECT COALESCE(SUM(quantidade * custo_unitario),0) AS gastos_pecas
      FROM movimentacao_estoque, mes
     WHERE tipo = 'ENTRADA' AND data >= mes.ini
),
sal AS (SELECT COALESCE(SUM(salario),0) AS gastos_salarios FROM funcionario WHERE status = 'ATIVO'),
ben AS (
    SELECT COALESCE(SUM(valor) FILTER (WHERE tipo = 'BENEFICIO'),0) AS gastos_beneficios,
           COALESCE(SUM(valor) FILTER (WHERE tipo = 'OUTRAS'),0)    AS outras_despesas
      FROM despesa, mes WHERE data >= mes.ini
)
SELECT fat.faturamento, fat.receita_servicos, fat.receita_pecas,
       gp.gastos_pecas, sal.gastos_salarios, ben.gastos_beneficios, ben.outras_despesas,
       fat.faturamento - (gp.gastos_pecas + sal.gastos_salarios
                          + ben.gastos_beneficios + ben.outras_despesas) AS lucro_estimado,
       (SELECT COUNT(*) FROM veiculo WHERE status IN ('EM_MANUTENCAO','TESTES_FINAIS','AGUARDANDO_PECAS')) AS veiculos_em_manutencao,
       (SELECT COUNT(*) FROM ordem_servico WHERE status IN ('ABERTA','EM_ANDAMENTO','AGUARDANDO_PECAS')) AS os_abertas,
       (SELECT COUNT(*) FROM ordem_servico, mes WHERE status = 'CONCLUIDA' AND data_conclusao >= mes.ini) AS os_concluidas
  FROM fat, gp, sal, ben;

-- Dados do gráfico de pizza, uma linha por categoria
CREATE OR REPLACE VIEW vw_dashboard_pizza AS
SELECT 'Receita de serviços' AS categoria, receita_servicos AS valor FROM vw_dashboard_resumo
UNION ALL SELECT 'Receita de peças',      receita_pecas      FROM vw_dashboard_resumo
UNION ALL SELECT 'Gastos com salários',   gastos_salarios    FROM vw_dashboard_resumo
UNION ALL SELECT 'Gastos com benefícios', gastos_beneficios  FROM vw_dashboard_resumo
UNION ALL SELECT 'Gastos com peças',      gastos_pecas       FROM vw_dashboard_resumo
UNION ALL SELECT 'Outras despesas',       outras_despesas    FROM vw_dashboard_resumo;

-- Dados do gráfico de linha; p_periodo aceita 'day', 'month' ou 'year'
CREATE OR REPLACE FUNCTION fn_dashboard_movimentacao(p_periodo TEXT, p_inicio DATE, p_fim DATE)
RETURNS TABLE (periodo DATE, veiculos_recebidos BIGINT, veiculos_entregues BIGINT,
               os_abertas BIGINT, os_concluidas BIGINT, faturamento NUMERIC) AS $$
    SELECT d::date,
           (SELECT COUNT(*) FROM veiculo v WHERE date_trunc(p_periodo, v.data_entrada) = d),
           (SELECT COUNT(*) FROM veiculo v WHERE date_trunc(p_periodo, v.data_entrega) = d),
           (SELECT COUNT(*) FROM ordem_servico o WHERE date_trunc(p_periodo, o.data_abertura) = d),
           (SELECT COUNT(*) FROM ordem_servico o
             WHERE o.status = 'CONCLUIDA' AND date_trunc(p_periodo, o.data_conclusao) = d),
           (SELECT COALESCE(SUM(o.valor_total),0) FROM ordem_servico o
             WHERE o.status = 'CONCLUIDA' AND date_trunc(p_periodo, o.data_conclusao) = d)
      FROM generate_series(date_trunc(p_periodo, p_inicio::timestamp),
                           date_trunc(p_periodo, p_fim::timestamp),
                           ('1 ' || p_periodo)::interval) AS d;
$$ LANGUAGE sql STABLE;
