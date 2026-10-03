package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest(properties = {"app.frete.loja-lat=0.0", "app.frete.loja-lng=0.0"})
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

    @MockitoBean
    private CepGeocoder cepGeocoder;

    @BeforeEach
    void prepararDados() {
        when(cepGeocoder.geocodificar(anyString()))
            .thenAnswer(invocation -> FreteTestCoordinates.paraCep(invocation.getArgument(0)));
        when(cepGeocoder.geocodificarOpcoes(anyString()))
            .thenAnswer(invocation -> List.of(cepGeocoder.geocodificar(invocation.getArgument(0))));
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
              "clienteTelefone": "(61) 99999-8888",
              "cep": "99990000",
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
            .andExpect(jsonPath("$.subtotal").value(99.80))
            .andExpect(jsonPath("$.frete").value(5.00))
            .andExpect(jsonPath("$.valorTotal").value(104.80));

        assertEquals(1, pedidoRepository.count());
        assertEquals(new BigDecimal("104.80"), pedidoRepository.findAll().get(0).getValorTotal());
    }

    @Test
    void geocodificaFreteAntesDeIniciarTransacaoDoPedido() throws Exception {
        when(cepGeocoder.geocodificarOpcoes(anyString())).thenAnswer(invocation -> {
            assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
            return List.of(new CepGeocoder.Coordenadas(0, 0, "teste", false));
        });

        String corpo = """
            {
              "clienteNome": "Cliente Público",
              "clienteTelefone": "61999998888",
              "cep": "99990000",
              "endereco": "Rua das Pizzas, 10",
              "itens": [{"produtoId": %d, "quantidade": 1}]
            }
            """.formatted(produto.getId());

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated());
    }

    @Test
    void consultaPedidoPublicoExigeCodigoETelefoneEExibeSomenteResumo() throws Exception {
        String corpo = """
            {
              "clienteNome": "Cliente Consulta",
              "clienteTelefone": "61988887777",
              "cep": "99990000",
              "endereco": "Rua da Consulta, 10",
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

        mockMvc.perform(get("/api/pedidos/consulta")
                .param("codigo", codigo)
                .param("telefone", "(61) 98888-7777"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(codigo))
            .andExpect(jsonPath("$[0].status").value("Pedido recebido"))
            .andExpect(jsonPath("$[0].itens.length()").value(1))
            .andExpect(jsonPath("$[0].subtotal").value(49.90))
            .andExpect(jsonPath("$[0].frete").value(5.00))
            .andExpect(jsonPath("$[0].valorTotal").value(54.90))
            .andExpect(jsonPath("$[0].clienteNome").doesNotExist())
            .andExpect(jsonPath("$[0].endereco").doesNotExist())
            .andExpect(jsonPath("$[0].clienteTelefone").doesNotExist())
            .andExpect(jsonPath("$[0].observacoes").doesNotExist());
        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", codigo))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/pedidos/consulta").param("telefone", "(61) 98888-7777"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/pedidos/consulta")
                .param("codigo", codigo)
                .param("telefone", "61999998888"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Pedido não encontrado"));
    }

    @Test
    void rejeitaPedidoPublicoComUsuarioId() throws Exception {
        Usuario usuario = usuarioRepository.saveAndFlush(new Usuario(
            "Usuário Existente", "existente@fatiaprime.test", "hash"));
        String corpo = """
            {
              "usuarioId": %d,
              "clienteNome": "Cliente Público",
              "clienteTelefone": "61999998888",
              "cep": "99990000",
              "endereco": "Rua das Pizzas, 10",
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
              "clienteTelefone": "61999998888",
              "cep": "99990000",
              "endereco": "Rua das Pizzas, 10",
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
    void rejeitaDadosDeClienteEQuantidadeInvalidos() throws Exception {
        String corpo = """
            {
              "clienteNome": "Cliente Público",
              "clienteTelefone": "123",
              "cep": "99990000",
              "endereco": "Rua das Pizzas, 10",
              "itens": [{"produtoId": %d, "quantidade": 51}]
            }
            """.formatted(produto.getId());

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listagemCompletaDePedidosContinuaProtegida() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
            .andExpect(status().isUnauthorized());
    }
}
