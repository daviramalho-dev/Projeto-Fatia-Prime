package com.example.Fatia.Prime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminOpcaoPizzaApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpcaoPizzaRepository opcaoPizzaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararDados() {
        opcaoPizzaRepository.deleteAll();
        usuarioRepository.deleteAll();
        opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Catupiry", TipoOpcaoPizza.BORDA, TipoProdutoPizza.SALGADA, new java.math.BigDecimal("7.00")));
        usuarioRepository.saveAndFlush(new Usuario(
            "Admin opções", "admin-opcoes@fatiaprime.test", passwordEncoder.encode("senha-123"), UsuarioRole.ADMIN));
    }

    @Test
    void publicoConsultaSomenteOpcoesAtivasDoTipoSolicitado() throws Exception {
        opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Chocolate", TipoOpcaoPizza.BORDA, TipoProdutoPizza.DOCE, new java.math.BigDecimal("6.00")));
        OpcaoPizza inativa = opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Cheddar", TipoOpcaoPizza.BORDA, TipoProdutoPizza.SALGADA, new java.math.BigDecimal("7.50")));
        inativa.setAtivo(false);
        opcaoPizzaRepository.saveAndFlush(inativa);

        mockMvc.perform(get("/api/opcoes-pizza").param("tipoProduto", "SALGADA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("Catupiry"));
        mockMvc.perform(get("/api/opcoes-pizza").param("tipoProduto", "DOCE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Chocolate"));
    }

    @Test
    void somenteAdministraOpcoesAutenticadoEConfiguraPrecoEAtivacao() throws Exception {
        mockMvc.perform(get("/api/admin/opcoes-pizza"))
            .andExpect(status().isUnauthorized());

        MockHttpSession session = adminSession();
        String corpo = """
            {"nome":"Cheddar","tipo":"BORDA","tipoProduto":"SALGADA","precoAdicional":7.50,"ativo":true}
            """;
        String resposta = mockMvc.perform(post("/api/admin/opcoes-pizza")
                .session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Cheddar"))
            .andExpect(jsonPath("$.precoAdicional").value(7.50))
            .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(resposta.replaceAll(".*\\\"id\\\":([0-9]+).*", "$1"));

        mockMvc.perform(put("/api/admin/opcoes-pizza/{id}", id)
                .session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Cheddar cremoso\",\"tipo\":\"BORDA\",\"tipoProduto\":\"SALGADA\",\"precoAdicional\":8.25,\"ativo\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Cheddar cremoso"))
            .andExpect(jsonPath("$.precoAdicional").value(8.25));

        mockMvc.perform(patch("/api/admin/opcoes-pizza/{id}/status", id)
                .session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"inativo\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ativo").value(false));
    }

    private MockHttpSession adminSession() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", "admin-opcoes@fatiaprime.test")
                .param("senha", "senha-123"))
            .andReturn().getRequest().getSession();
    }
}
