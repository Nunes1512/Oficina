-- Cria uma notificação para cada gerente e admin ativo; reutilizada pelos triggers e pelo back-end
CREATE OR REPLACE FUNCTION fn_notificar_gestao(p_tipo VARCHAR, p_msg VARCHAR) RETURNS void AS $$
    INSERT INTO notificacao (usuario_id, tipo, mensagem)
    SELECT u.id, p_tipo, p_msg
      FROM usuario u JOIN funcao r ON r.id = u.funcao_id
     WHERE u.ativo AND NOT u.bloqueado
       AND r.nome IN ('ROLE_GERENTE', 'ROLE_ADMIN_MASTER');
$$ LANGUAGE sql;

-- Alerta de estoque baixo, disparado só quando o saldo cruza o mínimo (evita alertas repetidos)
CREATE OR REPLACE FUNCTION fn_peca_estoque_baixo() RETURNS trigger AS $$
BEGIN
    PERFORM fn_notificar_gestao(
        'ESTOQUE_BAIXO',
        'Estoque baixo: ' || NEW.nome || ' (' || NEW.quantidade || ' un., mínimo ' || NEW.estoque_minimo || ')'
    );
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_peca_estoque_baixo
AFTER UPDATE OF quantidade ON peca
FOR EACH ROW
WHEN (NEW.quantidade <= NEW.estoque_minimo AND OLD.quantidade > NEW.estoque_minimo)
EXECUTE FUNCTION fn_peca_estoque_baixo();

-- Avisa a gestão sempre que um funcionário solicita uma peça
CREATE OR REPLACE FUNCTION fn_solicitacao_notifica() RETURNS trigger AS $$
BEGIN
    PERFORM fn_notificar_gestao('SOLICITACAO_PENDENTE',
        'Nova solicitação de peça pendente (#' || NEW.id || ')');
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_solicitacao_notifica
AFTER INSERT ON solicitacao_peca
FOR EACH ROW EXECUTE FUNCTION fn_solicitacao_notifica();
