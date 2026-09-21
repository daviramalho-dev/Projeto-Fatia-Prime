package com.example.Fatia.Prime;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public AdminBootstrap(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.admin.bootstrap-email:}") String email,
        @Value("${app.admin.bootstrap-password:}") String password,
        @Value("${app.admin.bootstrap-name:Administrador}") String name
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("As credenciais de bootstrap do administrador devem ser informadas juntas");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72 || password.length() < 6) {
            throw new IllegalStateException("A senha de bootstrap do administrador deve ter entre 6 e 72 bytes em UTF-8");
        }
        if (usuarioRepository.existsByRole(UsuarioRole.ADMIN)) return;

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("O e-mail de bootstrap já pertence a um usuário comum");
        }

        Usuario admin = new Usuario(
            name.trim(),
            normalizedEmail,
            passwordEncoder.encode(password),
            UsuarioRole.ADMIN
        );
        usuarioRepository.saveAndFlush(admin);
    }
}
