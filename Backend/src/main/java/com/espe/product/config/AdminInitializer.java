package com.espe.product.config;

import com.espe.product.entity.Role;
import com.espe.product.entity.Usuario;
import com.espe.product.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioRepository.existsByUsernameIgnoreCase(adminUsername)) {
            Usuario admin = new Usuario();
            admin.setUsername(adminUsername);

            String password = (adminPassword == null || adminPassword.isBlank()) ? "admin123" : adminPassword;
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            usuarioRepository.save(admin);
            System.out.println("Usuario administrador inicializado: " + adminUsername);
        }
    }
}
