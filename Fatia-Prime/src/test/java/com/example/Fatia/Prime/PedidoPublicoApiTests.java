package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class PedidoPublicoApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Produto produto;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();

        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("Carnes"));
        produto = produtoRepository.saveAndFlush(new Produto(
            "Calabresa Prime", "Calabresa e mozzarella", new BigDecimal("49.90"), null, categoria));
    }

    @Test
    void criaPedidoPublicoSemUsuarioECalculaTotalPeloBanco() throws Exception {
        String corpo = """
            {
              "clienteNome": "Cliente Público",
              "clienteEmail": "cliente@fatiaprime.test",
              "clienteTelefone": "(61) 99999-8888",
              "endereco": "Rua das Pizzas, 10",
              "observacoes": "Sem cebola",
              "itens": [{"produtoId": %d, "quantidade": 2}]
            }
            """.formatted(produto.getId());

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.usuarioId").doesNotExist())
            .andExpect(jsonPath("$.clienteNome").value("Cliente Público"))
            .andExpect(jsonPath("$.clienteTelefone").value("61999998888"))
            .andExpect(jsonPath("$.status").value("Pedido recebido"))
            .andExpect(jsonPath("$.valorTotal").value(99.80));

        assertEquals(1, pedidoRepository.count());
        assertEquals(new BigDecimal("99.80"), pedidoRepository.findAll().get(0).getValorTotal());
    }

    @Test
    void consultaPedidoPublicoPorCodigoEPorTelefone() throws Exception {
        String corpo = """
            {
              "clienteNome": "Cliente Consulta",
              "clienteEmail": "consulta@fatiaprime.test",
              "clienteTelefone": "61988887777",
              "itens": [{"produtoId": %d, "quantidade": 1}]
            }
            """.formatted(produto.getId());

        String resposta = mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        String codigo = resposta.replaceAll(".*\\\"codigo\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", codigo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].clienteNome").value("Cliente Consulta"));
        mockMvc.perform(get("/api/pedidos/consulta").param("telefone", "(61) 98888-7777"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(codigo));
    }

    @Test
    void rejeitaPedidoPublicoComUsuarioId() throws Exception {
        Usuario usuario = usuarioRepository.saveAndFlush(new Usuario(
            "Usuário Existente", "existente@fatiaprime.test", "hash"));
        String corpo = """
            {
              "usuarioId": %d,
              "clienteNome": "Cliente Público",
              "clienteEmail": "cliente@fatiaprime.test",
              "clienteTelefone": "61999998888",
              "itens": [{"produtoId": %d, "quantidade": 1}]
            }
            """.formatted(usuario.getId(), produto.getId());

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Pedidos públicos não podem informar usuário"));
    }

    @Test
    void rejeitaPedidoSemItens() throws Exception {
        String corpo = """
            {
              "clienteNome": "Cliente Público",
              "clienteEmail": "cliente@fatiaprime.test",
              "clienteTelefone": "61999998888",
              "itens": []
            }
            """;

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("O pedido deve conter pelo menos um item"));
    }

    @Test
    void listagemCompletaDePedidosContinuaProtegida() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
            .andExpect(status().isUnauthorized());
    }
}
