package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
    "app.admin.bootstrap-email=bootstrap@fatiaprime.test",
    "app.admin.bootstrap-password=senha-bootstrap-123",
    "app.admin.bootstrap-name=Administrador de Bootstrap"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminBootstrapTests {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void criaAdministradorConfiguradoSemExporSenha() {
        Usuario admin = usuarioRepository.findByEmailIgnoreCase("bootstrap@fatiaprime.test")
            .orElseThrow();

        assertEquals(UsuarioRole.ADMIN, admin.getRole());
        assertTrue(passwordEncoder.matches("senha-bootstrap-123", admin.getSenhaHash()));
        assertTrue(!admin.getSenhaHash().equals("senha-bootstrap-123"));
    }
}
