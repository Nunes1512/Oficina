package com.oficina.oficina_api.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.regex.Pattern;

/** CRUD dos módulos operacionais, usando a estrutura PostgreSQL definida em database/V1__tabelas.sql. */
@Service
public class CrudOficinaService {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;
    private static final Pattern IDENT = Pattern.compile("[a-z_][a-z0-9_]*");

    private record Resource(String table, Set<String> columns, String searchColumn) {}
    private final Map<String, Resource> resources = Map.ofEntries(
        Map.entry("clientes", resource("cliente", "nome cpf telefone email endereco", "nome")),
        Map.entry("veiculos", resource("veiculo", "marca modelo ano cor placa quilometragem data_entrada previsao_entrega data_entrega status cliente_id", "placa")),
        Map.entry("pecas", resource("peca", "nome fabricante quantidade valor_unitario estoque_minimo", "nome")),
        Map.entry("estoque", resource("peca", "nome fabricante quantidade valor_unitario estoque_minimo", "nome")),
        Map.entry("ordens-servico", resource("ordem_servico", "numero veiculo_id funcionario_id problema diagnostico valor_mao_obra valor_pecas valor_total status data_abertura data_conclusao", "numero")),
        Map.entry("itens-os", resource("item_os", "os_id peca_id quantidade valor_unitario", "os_id")),
        Map.entry("movimentacoes-estoque", resource("movimentacao_estoque", "peca_id tipo quantidade custo_unitario os_id observacao data", "observacao")),
        Map.entry("solicitacoes-pecas", resource("solicitacao_peca", "funcionario_id peca_id quantidade justificativa status data", "status")),
        Map.entry("despesas", resource("despesa", "tipo descricao valor data", "descricao")),
        Map.entry("funcionarios", resource("funcionario", "nome cpf telefone email cargo salario status usuario_id", "nome"))
    );

    public CrudOficinaService(JdbcTemplate jdbc, NamedParameterJdbcTemplate named) {
        this.jdbc = jdbc; this.named = named;
    }
    private static Resource resource(String table, String cols, String search) {
        return new Resource(table, Set.of(cols.split(" ")), search);
    }
    private Resource getResource(String name) {
        Resource r = resources.get(name);
        if (r == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo não encontrado: " + name);
        return r;
    }
    private String ident(String s) {
        if (!IDENT.matcher(s).matches()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de coluna inválido");
        return s;
    }
    private Map<String,Object> normalize(Map<String,Object> body, Resource r) {
        Map<String,Object> out = new LinkedHashMap<>();
        body.forEach((key,value) -> {
            String col = key.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
            if (!r.columns().contains(col)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo não permitido: " + key);
            out.put(ident(col), value);
        });
        if (out.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie pelo menos um campo");
        return out;
    }
    public List<Map<String,Object>> listar(String resourceName) {
        Resource r = getResource(resourceName);
        return jdbc.queryForList("SELECT * FROM " + ident(r.table()) + " ORDER BY id DESC");
    }
    public Map<String,Object> buscar(String resourceName, long id) {
        Resource r = getResource(resourceName);
        return jdbc.query("SELECT * FROM " + ident(r.table()) + " WHERE id = ?", rs -> {
            if (!rs.next()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado");
            var md = rs.getMetaData(); Map<String,Object> row = new LinkedHashMap<>();
            for (int i=1;i<=md.getColumnCount();i++) row.put(md.getColumnLabel(i), rs.getObject(i));
            return row;
        }, id);
    }
    public List<Map<String,Object>> pesquisar(String resourceName, String termo) {
        Resource r = getResource(resourceName);
        return jdbc.queryForList("SELECT * FROM " + ident(r.table()) + " WHERE CAST(" + ident(r.searchColumn()) + " AS TEXT) ILIKE ? ORDER BY id DESC", "%" + termo + "%");
    }
    public Map<String,Object> criar(String resourceName, Map<String,Object> body) {
        Resource r = getResource(resourceName); Map<String,Object> values = normalize(body, r);
        String cols = String.join(", ", values.keySet());
        String marks = String.join(", ", Collections.nCopies(values.size(), "?"));
        return jdbc.query("INSERT INTO " + ident(r.table()) + " (" + cols + ") VALUES (" + marks + ") RETURNING *", rs -> {
            if (!rs.next()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível criar registro");
            var md = rs.getMetaData(); Map<String,Object> row = new LinkedHashMap<>();
            for (int i=1;i<=md.getColumnCount();i++) row.put(md.getColumnLabel(i), rs.getObject(i)); return row;
        }, values.values().toArray());
    }
    public Map<String,Object> atualizar(String resourceName, long id, Map<String,Object> body) {
        Resource r = getResource(resourceName); Map<String,Object> values = normalize(body, r);
        String set = String.join(", ", values.keySet().stream().map(c -> c + " = ?").toList());
        List<Object> args = new ArrayList<>(values.values()); args.add(id);
        return jdbc.query("UPDATE " + ident(r.table()) + " SET " + set + " WHERE id = ? RETURNING *", rs -> {
            if (!rs.next()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado");
            var md = rs.getMetaData(); Map<String,Object> row = new LinkedHashMap<>();
            for (int i=1;i<=md.getColumnCount();i++) row.put(md.getColumnLabel(i), rs.getObject(i)); return row;
        }, args.toArray());
    }
    public void excluir(String resourceName, long id) {
        Resource r = getResource(resourceName);
        int count = jdbc.update("DELETE FROM " + ident(r.table()) + " WHERE id = ?", id);
        if (count == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado");
    }
    public List<Map<String,Object>> dashboardResumo() { return jdbc.queryForList("SELECT * FROM vw_dashboard_resumo"); }
    public List<Map<String,Object>> estoqueBaixo() { return jdbc.queryForList("SELECT * FROM vw_estoque_baixo ORDER BY quantidade"); }
    public List<Map<String,Object>> osAtrasadas() { return jdbc.queryForList("SELECT * FROM vw_os_atrasadas ORDER BY previsao_entrega"); }
    public List<Map<String,Object>> notificacoes(long usuarioId) { return jdbc.queryForList("SELECT * FROM notificacao WHERE usuario_id = ? ORDER BY data DESC", usuarioId); }
    public int marcarNotificacaoLida(long id) {
        int n = jdbc.update("UPDATE notificacao SET lida = TRUE WHERE id = ?", id);
        if (n == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"); return n;
    }
}
