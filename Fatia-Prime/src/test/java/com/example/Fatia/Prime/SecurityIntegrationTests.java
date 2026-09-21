package com.example.Fatia.Prime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTests {

    private static final String EMAIL = "admin@fatiaprime.test";
    private static final String PASSWORD = "senha-segura-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUsers() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void passwordEncoderUsesBcryptAndDoesNotStorePlainText() {
        String encoded = passwordEncoder.encode(PASSWORD);

        org.junit.jupiter.api.Assertions.assertNotEquals(PASSWORD, encoded);
        org.junit.jupiter.api.Assertions.assertTrue(encoded.startsWith("$2"));
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches(PASSWORD, encoded));
        org.junit.jupiter.api.Assertions.assertFalse(passwordEncoder.matches("senha-incorreta", encoded));
    }

    @Test
    void anonymousUserCannotAccessAdministrativeOrders() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void publicProductEndpointRemainsAccessibleAnonymously() throws Exception {
        mockMvc.perform(get("/api/produtos"))
            .andExpect(status().isOk());
    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        createUser();

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", EMAIL)
                .param("senha", "senha-incorreta"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(containsString("inválidos")));
    }

    @Test
    void validCredentialsAccessAdministrativeOrdersInSession() throws Exception {
        createUser();

        var session = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", EMAIL)
                .param("senha", PASSWORD))
            .andExpect(status().isNoContent())
            .andReturn()
            .getRequest()
            .getSession();

        mockMvc.perform(get("/api/pedidos").session((org.springframework.mock.web.MockHttpSession) session))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void logoutEndsAdministrativeSession() throws Exception {
        createUser();

        var session = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", EMAIL)
                .param("senha", PASSWORD))
            .andExpect(status().isNoContent())
            .andReturn()
            .getRequest()
            .getSession();

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .session((org.springframework.mock.web.MockHttpSession) session))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pedidos")
                .session((org.springframework.mock.web.MockHttpSession) session))
            .andExpect(status().isUnauthorized());
    }

    private void createUser() {
        usuarioRepository.saveAndFlush(new Usuario(
            "Administrador de Teste",
            EMAIL,
            passwordEncoder.encode(PASSWORD)
        ));
    }
}
