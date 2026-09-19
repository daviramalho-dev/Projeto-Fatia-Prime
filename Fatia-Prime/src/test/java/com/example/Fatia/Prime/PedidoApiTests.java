package com.example.Fatia.Prime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PedidoApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Usuario usuario;
    private Produto produto;
    private Produto produtoInativo;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = usuarioRepository.saveAndFlush(
            new Usuario("Cliente da API", "cliente-api@fatiaprime.test", "hash-de-teste")
        );
        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("API"));
        produto = produtoRepository.saveAndFlush(
            new Produto("Pizza API", "Produto persistido", new BigDecimal("24.90"), null, categoria)
        );
        produtoInativo = produtoRepository.saveAndFlush(
            new Produto("Pizza indisponível", "Produto inativo", new BigDecimal("30.00"), null, categoria)
        );
        produtoInativo.setAtivo(false);
        produtoRepository.saveAndFlush(produtoInativo);
    }

    @Test
    void criaPedidoPublicamenteERetornaContratoCompleto() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoPedido(usuario.getId(), produto.getId(), 2)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
            .andExpect(jsonPath("$.codigo").isString())
            .andExpect(jsonPath("$.codigo").value(org.hamcrest.Matchers.startsWith("FP-")))
            .andExpect(jsonPath("$.status").value("Pedido recebido"))
            .andExpect(jsonPath("$.valorTotal").value(49.80))
            .andExpect(jsonPath("$.itens[0].produtoId").value(produto.getId()))
            .andExpect(jsonPath("$.itens[0].quantidade").value(2))
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(24.90));
    }

    @Test
    void usuarioNaoAutenticadoNaoPodeConsultarPedidos() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoPedido(999999L, produto.getId(), 1)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    @Test
    void produtoInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoPedido(usuario.getId(), 999999L, 1)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Produto não encontrado"));
    }

    @Test
    void quantidadeInvalidaRetorna400() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoPedido(usuario.getId(), produto.getId(), 0)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("quantidade")));
    }

    @Test
    void pedidoVazioRetorna400() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "usuarioId": %d,
                      "itens": []
                    }
                    """.formatted(usuario.getId())))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("item")));
    }

    @Test
    void produtoInativoRetorna400() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoPedido(usuario.getId(), produtoInativo.getId(), 1)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Produto indisponível")));
    }

    @Test
    void valoresDoResponseSaoCalculadosPeloBackend() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoPedido(usuario.getId(), produto.getId(), 3)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.valorTotal").value(74.70))
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(24.90));
    }

    private String corpoPedido(Long usuarioId, Long produtoId, int quantidade) {
        return """
            {
              "usuarioId": %d,
              "observacoes": "Pedido pela API",
              "itens": [
                {
                  "produtoId": %d,
                  "quantidade": %d
                }
              ]
            }
            """.formatted(usuarioId, produtoId, quantidade);
    }
}