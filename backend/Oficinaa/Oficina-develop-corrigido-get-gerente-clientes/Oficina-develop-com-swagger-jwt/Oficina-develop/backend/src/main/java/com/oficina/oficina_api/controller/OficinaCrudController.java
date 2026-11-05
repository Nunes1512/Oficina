
package com.oficina.oficina_api.controller;

import com.oficina.oficina_api.service.CrudOficinaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gerente")
@PreAuthorize("hasAnyRole('ADMIN_MASTER', 'GERENTE')")
public class OficinaCrudController {

    private final CrudOficinaService service;
    private final JdbcTemplate jdbc;

    public OficinaCrudController(
            CrudOficinaService service,
            JdbcTemplate jdbc) {
        this.service = service;
        this.jdbc = jdbc;
    }

    // Valida os módulos que o gerente pode acessar
    private void validarModuloGerente(String modulo) {
        if ("funcionarios".equals(modulo)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente o Administrador Master pode gerenciar funcionários"
            );
        }
    }

    // LISTAR REGISTROS
    @GetMapping("/{modulo}")
    public List<Map<String, Object>> listar(
            @PathVariable String modulo) {

        validarModuloGerente(modulo);
        return service.listar(modulo);
    }

    // BUSCAR REGISTRO POR ID
    @GetMapping("/{modulo}/{id}")
    public Map<String, Object> buscar(
            @PathVariable String modulo,
            @PathVariable long id) {

        validarModuloGerente(modulo);
        return service.buscar(modulo, id);
    }

    // PESQUISAR REGISTROS
    @GetMapping("/{modulo}/pesquisar")
    public List<Map<String, Object>> pesquisar(
            @PathVariable String modulo,
            @RequestParam String q) {

        validarModuloGerente(modulo);
        return service.pesquisar(modulo, q);
    }

    // CRIAR REGISTRO
    @PostMapping("/{modulo}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> criar(
            @PathVariable String modulo,
            @RequestBody Map<String, Object> body) {

        validarModuloGerente(modulo);
        return service.criar(modulo, body);
    }

    // ATUALIZAR REGISTRO
    @PutMapping("/{modulo}/{id}")
    public Map<String, Object> atualizar(
            @PathVariable String modulo,
            @PathVariable long id,
            @RequestBody Map<String, Object> body) {

        validarModuloGerente(modulo);
        return service.atualizar(modulo, id, body);
    }

    // EXCLUIR REGISTRO
    @DeleteMapping("/{modulo}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable String modulo,
            @PathVariable long id) {

        validarModuloGerente(modulo);
        service.excluir(modulo, id);
    }

    // BUSCAR CLIENTE POR CPF
    @GetMapping("/clientes/por-cpf/{cpf}")
    public List<Map<String, Object>> clientePorCpf(
            @PathVariable String cpf) {

        return jdbc.queryForList(
                "SELECT * FROM cliente WHERE cpf = ?",
                cpf
        );
    }

    // BUSCAR VEÍCULO POR PLACA
    @GetMapping("/veiculos/por-placa/{placa}")
    public List<Map<String, Object>> veiculoPorPlaca(
            @PathVariable String placa) {

        return jdbc.queryForList(
                "SELECT * FROM veiculo WHERE UPPER(placa) = UPPER(?)",
                placa
        );
    }

    // HISTÓRICO DE VEÍCULOS DO CLIENTE
    @GetMapping("/clientes/{id}/veiculos")
    public List<Map<String, Object>> historicoVeiculos(
            @PathVariable long id) {

        return jdbc.queryForList(
                "SELECT * FROM veiculo WHERE cliente_id = ? ORDER BY data_entrada DESC",
                id
        );
    }

    // LINHA DO TEMPO DO VEÍCULO
    @GetMapping("/veiculos/{id}/timeline")
    public List<Map<String, Object>> timeline(
            @PathVariable long id) {

        return jdbc.queryForList(
                "SELECT * FROM timeline_veiculo WHERE veiculo_id = ? ORDER BY data",
                id
        );
    }

    // DASHBOARD - RESUMO
    @GetMapping("/dashboard/resumo")
    public List<Map<String, Object>> dashboard() {
        return service.dashboardResumo();
    }

    // DASHBOARD - ESTOQUE BAIXO
    @GetMapping("/dashboard/estoque-baixo")
    public List<Map<String, Object>> estoqueBaixo() {
        return service.estoqueBaixo();
    }

    // DASHBOARD - ORDENS DE SERVIÇO ATRASADAS
    @GetMapping("/dashboard/os-atrasadas")
    public List<Map<String, Object>> osAtrasadas() {
        return service.osAtrasadas();
    }

    // LISTAR NOTIFICAÇÕES
    @GetMapping("/notificacoes/usuario/{usuarioId}")
    public List<Map<String, Object>> notificacoes(
            @PathVariable long usuarioId) {

        return service.notificacoes(usuarioId);
    }

    // MARCAR NOTIFICAÇÃO COMO LIDA
    @PatchMapping("/notificacoes/{id}/lida")
    public Map<String, Object> marcarLida(
            @PathVariable long id) {

        service.marcarNotificacaoLida(id);

        return Map.of(
                "mensagem", "Notificação marcada como lida",
                "id", id
        );
    }
}
