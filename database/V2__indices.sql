-- Aceleram os joins por chave estrangeira
CREATE INDEX idx_veiculo_cliente      ON veiculo(cliente_id);
CREATE INDEX idx_os_veiculo           ON ordem_servico(veiculo_id);
CREATE INDEX idx_os_funcionario       ON ordem_servico(funcionario_id);
CREATE INDEX idx_item_os_os           ON item_os(os_id);

-- Aceleram os filtros de status usados nas listagens e nos cards do dashboard
CREATE INDEX idx_veiculo_status       ON veiculo(status);
CREATE INDEX idx_os_status            ON ordem_servico(status);
CREATE INDEX idx_solicitacao_status   ON solicitacao_peca(status);

-- Aceleram as consultas por período do gráfico de movimentação
CREATE INDEX idx_os_abertura          ON ordem_servico(data_abertura);
CREATE INDEX idx_os_conclusao         ON ordem_servico(data_conclusao);

-- Aceleram o histórico de estoque, as notificações não lidas e a timeline
CREATE INDEX idx_mov_peca             ON movimentacao_estoque(peca_id, data);
CREATE INDEX idx_notificacao_usuario  ON notificacao(usuario_id, lida);
CREATE INDEX idx_timeline_veiculo     ON timeline_veiculo(veiculo_id, data);
