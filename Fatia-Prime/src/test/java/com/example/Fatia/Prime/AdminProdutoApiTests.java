package com.example.Fatia.Prime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminProdutoApiTests {

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

    private Categoria carnes;
    private Categoria queijos;
    private Produto produtoAtivo;
    private Produto produtoInativo;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();

        usuarioRepository.saveAndFlush(new Usuario(
            "Administrador de Produtos",
            "admin-produtos@fatiaprime.test",
            passwordEncoder.encode("senha-123"),
            UsuarioRole.ADMIN
        ));

        carnes = categoriaRepository.saveAndFlush(new Categoria("Carnes"));
        queijos = categoriaRepository.saveAndFlush(new Categoria("Queijos"));
        produtoAtivo = produtoRepository.saveAndFlush(new Produto(
            "Calabresa Prime", "Calabresa e mozzarella", new BigDecimal("49.90"), "calabresa.jpg", carnes));
        produtoInativo = produtoRepository.saveAndFlush(new Produto(
            "Quatro Queijos", "Mozzarella e provolone", new BigDecimal("56.90"), null, queijos));
        produtoInativo.setAtivo(false);
        produtoRepository.saveAndFlush(produtoInativo);
    }

    @Test
    void listaProdutosAutenticadoIncluiAtivosEInativos() throws Exception {
        mockMvc.perform(get("/api/admin/produtos").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listaProdutosSemAutenticacaoRetorna401() throws Exception {
        mockMvc.perform(get("/api/admin/produtos"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void filtraPorNomeCategoriaEStatus() throws Exception {
        mockMvc.perform(get("/api/admin/produtos")
                .param("nome", "calabresa")
                .param("categoriaId", carnes.getId().toString())
                .param("status", "ativo")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nome").value("Calabresa Prime"));

        mockMvc.perform(get("/api/admin/produtos").param("status", "inativo").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].ativo").value(false));
    }

    @Test
    void detalhaProdutoExistenteEProdutoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/admin/produtos/{id}", produtoAtivo.getId()).session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(produtoAtivo.getId()))
            .andExpect(jsonPath("$.categoriaNome").value("Carnes"))
            .andExpect(jsonPath("$.imagem").value("calabresa.jpg"));

        mockMvc.perform(get("/api/admin/produtos/999999").session(adminSession()))
            .andExpect(status().isNotFound());
    }

    @Test
    void criaProdutoValidoEContinuaPersistido() throws Exception {
        String corpo = """
            {
              "nome": "Frango Prime",
              "descricao": "Frango e catupiry",
              "preco": 54.90,
              "imagem": "frango.jpg",
              "categoriaId": %d,
              "ativo": true
            }
            """.formatted(queijos.getId());

        String resposta = mockMvc.perform(post("/api/admin/produtos")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Frango Prime"))
            .andExpect(jsonPath("$.categoriaId").value(queijos.getId()))
            .andReturn().getResponse().getContentAsString();

        assertNotNull(resposta);
        Produto salvo = produtoRepository.findAll().stream()
            .filter(produto -> "Frango Prime".equals(produto.getNome()))
            .findFirst().orElseThrow();
        assertEquals(new BigDecimal("54.90"), salvo.getPreco());
    }

    @Test
    void rejeitaDadosInvalidosECategoriaInexistente() throws Exception {
        mockMvc.perform(post("/api/admin/produtos")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\" \",\"preco\":-1,\"categoriaId\":1}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("O nome do produto é obrigatório")));

        mockMvc.perform(post("/api/admin/produtos")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Pizza válida\",\"preco\":10,\"categoriaId\":999999}"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Categoria não encontrada")));

        mockMvc.perform(post("/api/admin/produtos")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Pizza sem preço\",\"preco\":0,\"categoriaId\":" + carnes.getId() + "}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("O preço deve ser maior que zero")));
    }

    @Test
    void editaProdutoPreservandoId() throws Exception {
        Long id = produtoAtivo.getId();
        String corpo = """
            {
              "nome": "Calabresa Especial",
              "descricao": "Nova descrição",
              "preco": 59.90,
              "imagem": "nova.jpg",
              "categoriaId": %d,
              "ativo": true
            }
            """.formatted(queijos.getId());

        mockMvc.perform(put("/api/admin/produtos/{id}", id)
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.nome").value("Calabresa Especial"))
            .andExpect(jsonPath("$.categoriaNome").value("Queijos"));

        assertEquals(id, produtoRepository.findById(id).orElseThrow().getId());
        mockMvc.perform(put("/api/admin/produtos/999999")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isNotFound());
    }

    @Test
    void desativaEReativaSemExcluirProduto() throws Exception {
        Long id = produtoAtivo.getId();

        mockMvc.perform(patch("/api/admin/produtos/{id}/status", id)
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"inativo\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(get("/api/produtos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(id.intValue()))));

        assertFalse(produtoRepository.findById(id).orElseThrow().isAtivo());

        mockMvc.perform(get("/api/admin/produtos/{id}", id).session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(patch("/api/admin/produtos/{id}/status", id)
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ativo\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void detalhePublicoNaoRetornaProdutoInativo() throws Exception {
        produtoInativo.setAtivo(false);
        produtoRepository.saveAndFlush(produtoInativo);

        mockMvc.perform(get("/api/produtos/{id}", produtoInativo.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void statusInvalidoRetorna400() throws Exception {
        mockMvc.perform(patch("/api/admin/produtos/{id}/status", produtoAtivo.getId())
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"arquivado\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Status deve ser ativo ou inativo")));
    }

    private MockHttpSession adminSession() throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", "admin-produtos@fatiaprime.test")
                .param("senha", "senha-123"))
            .andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }
}