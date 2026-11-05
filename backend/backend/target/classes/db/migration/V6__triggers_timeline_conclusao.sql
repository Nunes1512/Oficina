-- Registra a entrada do veículo na timeline assim que ele é cadastrado
CREATE OR REPLACE FUNCTION fn_veiculo_timeline_insert() RETURNS trigger AS $$
BEGIN
    INSERT INTO timeline_veiculo (veiculo_id, evento, descricao)
    VALUES (NEW.id, 'ENTRADA', 'Veículo ' || NEW.placa || ' deu entrada na oficina');
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_veiculo_timeline_insert
AFTER INSERT ON veiculo
FOR EACH ROW EXECUTE FUNCTION fn_veiculo_timeline_insert();

-- Registra cada mudança de status na timeline, preenche a data de entrega
-- e avisa a gestão quando o veículo fica aguardando peças.
-- É BEFORE para poder alterar data_entrega na própria linha
CREATE OR REPLACE FUNCTION fn_veiculo_timeline_status() RETURNS trigger AS $$
DECLARE
    v_evento VARCHAR(30);
BEGIN
    v_evento := CASE NEW.status
        WHEN 'EM_MANUTENCAO' THEN 'INICIO_CONSERTO'
        WHEN 'CONCLUIDO'     THEN 'CONCLUSAO'
        WHEN 'ENTREGUE'      THEN 'ENTREGA'
        ELSE 'MUDANCA_STATUS'
    END;

    INSERT INTO timeline_veiculo (veiculo_id, evento, descricao)
    VALUES (NEW.id, v_evento, 'Status alterado de ' || OLD.status || ' para ' || NEW.status);

    IF NEW.status = 'ENTREGUE' AND NEW.data_entrega IS NULL THEN
        NEW.data_entrega := now();
    END IF;

    IF NEW.status = 'AGUARDANDO_PECAS' THEN
        PERFORM fn_notificar_gestao('VEICULO_AGUARDANDO_PECA',
            'Veículo ' || NEW.placa || ' aguardando peças');
    END IF;
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_veiculo_timeline_status
BEFORE UPDATE OF status ON veiculo
FOR EACH ROW WHEN (OLD.status IS DISTINCT FROM NEW.status)
EXECUTE FUNCTION fn_veiculo_timeline_status();

-- Ao concluir a OS: preenche a data de conclusão e notifica a gestão
CREATE OR REPLACE FUNCTION fn_os_concluida() RETURNS trigger AS $$
BEGIN
    IF NEW.data_conclusao IS NULL THEN
        NEW.data_conclusao := now();
    END IF;
    PERFORM fn_notificar_gestao('SERVICO_CONCLUIDO', 'OS ' || NEW.numero || ' concluída');
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_os_concluida
BEFORE UPDATE OF status ON ordem_servico
FOR EACH ROW WHEN (NEW.status = 'CONCLUIDA' AND OLD.status <> 'CONCLUIDA')
EXECUTE FUNCTION fn_os_concluida();
