package com.oficina.oficina_api.config;

import com.oficina.oficina_api.model.Role;
import com.oficina.oficina_api.model.Usuario;
import com.oficina.oficina_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminMasterInitializer {

    @Bean
    CommandLineRunner criarAdminMaster(
            UsuarioRepository repository,
            PasswordEncoder encoder,
            @Value("${app.admin.login:admin}") String login,
            @Value("${app.admin.password:ALTERE_AQUI}") String password) {

        return args -> {
            if (repository.findByLogin(login).isEmpty()) {
                Usuario admin = new Usuario(
                        "Administrador Master",
                        login,
                        encoder.encode(password),
                        Role.ADMIN_MASTER,
                        true
                );
                repository.save(admin);
                System.out.println("Administrador Master criado: " + login);
            }
        };
    }
}
