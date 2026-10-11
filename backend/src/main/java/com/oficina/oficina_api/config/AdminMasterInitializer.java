package com.oficina.oficina_api.config;

import com.oficina.oficina_api.model.Funcao;
import com.oficina.oficina_api.model.Usuario;
import com.oficina.oficina_api.repository.FuncaoRepository;
import com.oficina.oficina_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminMasterInitializer {
    @Bean
    CommandLineRunner criarAdminMaster(UsuarioRepository usuarios, FuncaoRepository funcoes,
            PasswordEncoder encoder,
            @Value("${app.admin.login:admin@oficina.local}") String login,
            @Value("${app.admin.password:}") String password) {
        return args -> {
            if (password == null || password.isBlank() || password.equals("ALTERE_AQUI")) {
                System.out.println("Admin Master não criado: configure app.admin.password em application.properties.");
                return;
            }
            if (usuarios.findByEmail(login).isEmpty()) {
                Funcao funcao = funcoes.findByNome("ROLE_ADMIN_MASTER")
                    .orElseThrow(() -> new IllegalStateException("Perfil ROLE_ADMIN_MASTER não encontrado. Execute V3__dados_iniciais.sql."));
                usuarios.save(new Usuario(login, encoder.encode(password), funcao));
                System.out.println("Administrador Master criado: " + login);
            }
        };
    }
}
