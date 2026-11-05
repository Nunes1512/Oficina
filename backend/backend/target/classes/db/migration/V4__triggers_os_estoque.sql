-- Mantém o valor total da OS sempre igual a mão de obra + peças
CREATE OR REPLACE FUNCTION fn_os_calcula_total() RETURNS trigger AS $$
BEGIN
    NEW.valor_total := NEW.valor_mao_obra + NEW.valor_pecas;
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_os_calcula_total
BEFORE INSERT OR UPDATE OF valor_mao_obra, valor_pecas ON ordem_servico
FOR EACH ROW EXECUTE FUNCTION fn_os_calcula_total();

-- Ao usar uma peça na OS: bloqueia se faltar saldo, baixa o estoque,
-- registra a saída no histórico e recalcula o valor das peças da OS
CREATE OR REPLACE FUNCTION fn_item_os_baixa_estoque() RETURNS trigger AS $$
DECLARE
    v_saldo INT;
BEGIN
    SELECT quantidade INTO v_saldo FROM peca WHERE id = NEW.peca_id FOR UPDATE;

    IF v_saldo < NEW.quantidade THEN
        RAISE EXCEPTION 'Estoque insuficiente para a peça % (saldo: %, solicitado: %)',
            NEW.peca_id, v_saldo, NEW.quantidade;
    END IF;

    UPDATE peca SET quantidade = quantidade - NEW.quantidade WHERE id = NEW.peca_id;

    INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, os_id, observacao)
    VALUES (NEW.peca_id, 'SAIDA', NEW.quantidade, NEW.os_id, 'Utilizada na OS');

    UPDATE ordem_servico
       SET valor_pecas = (SELECT COALESCE(SUM(quantidade * valor_unitario), 0)
                            FROM item_os WHERE os_id = NEW.os_id)
     WHERE id = NEW.os_id;

    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_item_os_baixa_estoque
AFTER INSERT ON item_os
FOR EACH ROW EXECUTE FUNCTION fn_item_os_baixa_estoque();
