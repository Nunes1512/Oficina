package com.oficina.oficina_api.controller;

import com.oficina.oficina_api.service.CrudOficinaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/** Endpoints dos módulos de operação. Todos os dados são persistidos no PostgreSQL. */
@RestController
@RequestMapping("/gerente")
@PreAuthorize("hasAnyRole('ADMIN_MASTER','GERENTE')")
public class OficinaCrudController {
    private final CrudOficinaService service;
    private final JdbcTemplate jdbc;
    public OficinaCrudController(CrudOficinaService service, JdbcTemplate jdbc) { this.service = service; this.jdbc = jdbc; }

    private void validarModuloGerente(String modulo) { if ("funcionarios".equals(modulo)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o Administrador Master pode gerenciar funcionários"); }
    @GetMapping("/{modulo}") public List<Map<String,Object>> listar(@PathVariable String modulo) { validarModuloGerente(modulo); return service.listar(modulo); }
    @GetMapping("/{modulo}/{id}") public Map<String,Object> buscar(@PathVariable String modulo, @PathVariable long id) { validarModuloGerente(modulo); return service.buscar(modulo,id); }
    @GetMapping("/{modulo}/pesquisar") public List<Map<String,Object>> pesquisar(@PathVariable String modulo, @RequestParam String q) { validarModuloGerente(modulo); return service.pesquisar(modulo,q); }
    @PostMapping("/{modulo}") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> criar(@PathVariable String modulo, @RequestBody Map<String,Object> body) { validarModuloGerente(modulo); return service.criar(modulo,body); }
    @PutMapping("/{modulo}/{id}") public Map<String,Object> atualizar(@PathVariable String modulo, @PathVariable long id, @RequestBody Map<String,Object> body) { validarModuloGerente(modulo); return service.atualizar(modulo,id,body); }
    @DeleteMapping("/{modulo}/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String modulo, @PathVariable long id) { validarModuloGerente(modulo); service.excluir(modulo,id); }

    @GetMapping("/clientes/por-cpf/{cpf}")
    public List<Map<String,Object>> clientePorCpf(@PathVariable String cpf) { return jdbc.queryForList("SELECT * FROM cliente WHERE cpf = ?", cpf); }
    @GetMapping("/veiculos/por-placa/{placa}")
    public List<Map<String,Object>> veiculoPorPlaca(@PathVariable String placa) { return jdbc.queryForList("SELECT * FROM veiculo WHERE UPPER(placa) = UPPER(?)", placa); }
    @GetMapping("/clientes/{id}/veiculos")
    public List<Map<String,Object>> historicoVeiculos(@PathVariable long id) { return jdbc.queryForList("SELECT * FROM veiculo WHERE cliente_id = ? ORDER BY data_entrada DESC", id); }
    @GetMapping("/veiculos/{id}/timeline")
    public List<Map<String,Object>> timeline(@PathVariable long id) { return jdbc.queryForList("SELECT * FROM timeline_veiculo WHERE veiculo_id = ? ORDER BY data", id); }
    @GetMapping("/dashboard/resumo") public List<Map<String,Object>> dashboard() { return service.dashboardResumo(); }
    @GetMapping("/dashboard/estoque-baixo") public List<Map<String,Object>> estoqueBaixo() { return service.estoqueBaixo(); }
    @GetMapping("/dashboard/os-atrasadas") public List<Map<String,Object>> osAtrasadas() { return service.osAtrasadas(); }
    @GetMapping("/notificacoes/usuario/{usuarioId}") public List<Map<String,Object>> notificacoes(@PathVariable long usuarioId) { return service.notificacoes(usuarioId); }
    @PatchMapping("/notificacoes/{id}/lida") public Map<String,Object> marcarLida(@PathVariable long id) {
        service.marcarNotificacaoLida(id); return Map.of("mensagem", "Notificação marcada como lida", "id", id);
    }
}
