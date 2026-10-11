package com.oficina.oficina_api.service;

import com.oficina.oficina_api.model.Usuario;
import com.oficina.oficina_api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) { this.repository = repository; }

    public Usuario buscarPorLogin(String login) {
        return repository.findByEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        Usuario usuario = buscarPorLogin(login);
        String role = usuario.getFuncao().getNome().replaceFirst("^ROLE_", "");
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .roles(role)
                .disabled(!usuario.isAtivo())
                .accountLocked(usuario.isBloqueado())
                .build();
    }
}
