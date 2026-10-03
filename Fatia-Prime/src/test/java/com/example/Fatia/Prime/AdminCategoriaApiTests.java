package com.example.Fatia.Prime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
class AdminCategoriaApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Categoria categoria;
    private Produto produto;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuarioRepository.saveAndFlush(new Usuario(
            "Administrador de Categorias",
            "admin-categorias@fatiaprime.test",
            passwordEncoder.encode("senha-123"),
            UsuarioRole.ADMIN
        ));
        usuarioRepository.saveAndFlush(new Usuario(
            "Usuário Comum",
            "usuario-categorias@fatiaprime.test",
            passwordEncoder.encode("senha-123"),
            UsuarioRole.USER
        ));

        categoria = categoriaRepository.saveAndFlush(new Categoria("Carnes"));
        produto = produtoRepository.saveAndFlush(new Produto(
            "Calabresa Prime",
            "Calabresa e mozzarella",
            new BigDecimal("49.90"),
            null,
            categoria
        ));
    }

    @Test
    void getPublicosListamEBuscamCategorias() throws Exception {
        mockMvc.perform(get("/api/categorias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(categoria.getId()))
            .andExpect(jsonPath("$[0].nome").value("Carnes"));

        mockMvc.perform(get("/api/categorias/{id}", categoria.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(categoria.getId()))
            .andExpect(jsonPath("$.nome").value("Carnes"));
    }

    @Test
    void criaCategoriaValidaComoAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/categorias")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"  Doces  \"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Doces"));

        org.junit.jupiter.api.Assertions.assertTrue(categoriaRepository.existsByNomeIgnoreCase("Doces"));
        mockMvc.perform(get("/api/admin/categorias").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/admin/categorias/{id}", categoria.getId()).session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Carnes"));
    }

    @Test
    void rejeitaNomeVazioENomeAcimaDoLimite() throws Exception {
        mockMvc.perform(post("/api/admin/categorias")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("obrigatório")));

        mockMvc.perform(post("/api/admin/categorias")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"" + "a".repeat(101) + "\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("100 caracteres")));
    }

    @Test
    void rejeitaCategoriaDuplicadaSemDiferenciarMaiusculas() throws Exception {
        mockMvc.perform(post("/api/admin/categorias")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\" carnes \"}"))
            .andExpect(status().isConflict())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Já existe")));
    }

    @Test
    void editaCategoriaComoAdminERejeitaDuplicidade() throws Exception {
        mockMvc.perform(put("/api/admin/categorias/{id}", categoria.getId())
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Clássicas\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(categoria.getId()))
            .andExpect(jsonPath("$.nome").value("Clássicas"));

        Categoria outra = categoriaRepository.saveAndFlush(new Categoria("Bebidas"));
        mockMvc.perform(put("/api/admin/categorias/{id}", categoria.getId())
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"bebidas\"}"))
            .andExpect(status().isConflict());

        org.junit.jupiter.api.Assertions.assertEquals("Clássicas",
            categoriaRepository.findById(categoria.getId()).orElseThrow().getNome());
        org.junit.jupiter.api.Assertions.assertNotNull(outra.getId());
    }

    @Test
    void excluiCategoriaSemProdutosComoAdmin() throws Exception {
        Categoria vazia = categoriaRepository.saveAndFlush(new Categoria("Temporária"));

        mockMvc.perform(delete("/api/admin/categorias/{id}", vazia.getId())
                .session(adminSession()).with(csrf()))
            .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(categoriaRepository.existsById(vazia.getId()));
    }

    @Test
    void naoExcluiCategoriaAssociadaAProduto() throws Exception {
        mockMvc.perform(delete("/api/admin/categorias/{id}", categoria.getId())
                .session(adminSession()).with(csrf()))
            .andExpect(status().isConflict())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("vinculada a produtos")));

        org.junit.jupiter.api.Assertions.assertTrue(categoriaRepository.existsById(categoria.getId()));
        org.junit.jupiter.api.Assertions.assertEquals(categoria.getId(), produtoRepository.findById(produto.getId())
            .orElseThrow().getCategoria().getId());
    }

    @Test
    void operacoesDeEscritaNegamAcessoAnonimoEUsuarioComum() throws Exception {
        assertOperacoesEscrita(null, 401);
        assertOperacoesEscrita(usuarioSession(), 403);
    }

    @Test
    void endpointsAdministrativosDeConsultaExigemAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/categorias"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/categorias/{id}", categoria.getId()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/categorias").session(usuarioSession()))
            .andExpect(status().isForbidden());
    }

    private void assertOperacoesEscrita(MockHttpSession session, int statusCode) throws Exception {
        var create = post("/api/admin/categorias")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Nova\"}");
        var edit = put("/api/admin/categorias/{id}", categoria.getId())
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Editada\"}");
        var delete = delete("/api/admin/categorias/{id}", categoria.getId()).with(csrf());

        var createRequest = session == null ? create : create.session(session);
        var editRequest = session == null ? edit : edit.session(session);
        var deleteRequest = session == null ? delete : delete.session(session);
        mockMvc.perform(createRequest).andExpect(status().is(statusCode));
        mockMvc.perform(editRequest).andExpect(status().is(statusCode));
        mockMvc.perform(deleteRequest).andExpect(status().is(statusCode));
    }

    private MockHttpSession usuarioSession() throws Exception {
        return login("usuario-categorias@fatiaprime.test");
    }

    private MockHttpSession adminSession() throws Exception {
        return login("admin-categorias@fatiaprime.test");
    }

    private MockHttpSession login(String email) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", email)
                .param("senha", "senha-123"))
            .andExpect(status().isNoContent())
            .andReturn()
            .getRequest()
            .getSession();
    }
}
