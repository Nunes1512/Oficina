package com.oficina.oficina_api.controller;

import com.oficina.oficina_api.dto.LoginRequest;
import com.oficina.oficina_api.dto.LoginResponse;
import com.oficina.oficina_api.model.Usuario;
import com.oficina.oficina_api.service.JwtService;
import com.oficina.oficina_api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UsuarioService usuarioService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.senha()));

        Usuario usuario = usuarioService.buscarPorLogin(request.login());
        String token = jwtService.gerarToken(usuario.getLogin(), usuario.getRole().name());

        return ResponseEntity.ok(
                new LoginResponse(token, "Bearer", usuario.getLogin(), usuario.getRole().name()));
    }
}
