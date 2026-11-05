package com.oficina.oficina_api.controller;

import com.oficina.oficina_api.service.CrudOficinaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/** Cadastro de funcionários restrito ao Administrador Master. */
@RestController
@RequestMapping("/admin/funcionarios")
@PreAuthorize("hasRole('ADMIN_MASTER')")
public class AdminFuncionarioController {
    private final CrudOficinaService service;
    public AdminFuncionarioController(CrudOficinaService service) { this.service = service; }
    @GetMapping public List<Map<String,Object>> listar() { return service.listar("funcionarios"); }
    @GetMapping("/{id}") public Map<String,Object> buscar(@PathVariable long id) { return service.buscar("funcionarios", id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> criar(@RequestBody Map<String,Object> body) { return service.criar("funcionarios", body); }
    @PutMapping("/{id}") public Map<String,Object> atualizar(@PathVariable long id, @RequestBody Map<String,Object> body) { return service.atualizar("funcionarios", id, body); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable long id) { service.excluir("funcionarios", id); }
}
