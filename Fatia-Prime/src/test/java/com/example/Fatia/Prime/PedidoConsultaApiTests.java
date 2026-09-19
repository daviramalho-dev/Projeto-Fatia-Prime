package com.example.Fatia.Prime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PedidoConsultaApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Usuario usuario;
    private Pedido pedido;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario("Cliente da Consulta", "consulta@fatiaprime.test", "hash-de-teste");
        usuario.setTelefone("(61) 98765-4321");
        usuario = usuarioRepository.saveAndFlush(usuario);

        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("Consulta"));
        Produto produto = produtoRepository.saveAndFlush(
            new Produto("Pizza Consulta", "Produto consultável", new BigDecimal("24.90"), null, categoria)
        );
        pedido = pedidoService.criar(new PedidoRequest(
            usuario.getId(),
            "Observação da consulta",
            java.util.List.of(new ItemPedidoRequest(produto.getId(), 2))
        ));
    }

    @Test
    void consultaPublicaPorCodigoRetornaDadosDoPedidoSemSenha() throws Exception {
        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", pedido.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(pedido.getId()))
            .andExpect(jsonPath("$[0].codigo").value(pedido.getCodigo()))
            .andExpect(jsonPath("$[0].status").value("Pedido recebido"))
            .andExpect(jsonPath("$[0].valorTotal").value(49.80))
            .andExpect(jsonPath("$[0].telefone").value("(61) 98765-4321"))
            .andExpect(jsonPath("$[0].itens[0].quantidade").value(2))
            .andExpect(jsonPath("$[0].itens[0].precoUnitario").value(24.90))
            .andExpect(jsonPath("$[0].itens[0].subtotal").value(49.80))
            .andExpect(jsonPath("$[0].senhaHash").doesNotExist())
            .andExpect(jsonPath("$[0].senha_hash").doesNotExist());
    }

    @Test
    void consultaPorTelefoneAceitaFormatoComMascara() throws Exception {
        mockMvc.perform(get("/api/pedidos/consulta").param("telefone", "61987654321"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].codigo").value(pedido.getCodigo()));
    }

    @Test
    void consultaPorTelefoneFuncionaParaPedidoPublicoSemUsuario() throws Exception {
        Categoria categoria = categoriaRepository.findAll().get(0);
        Produto produto = produtoRepository.saveAndFlush(
            new Produto("Pizza Pública", "Produto público", new BigDecimal("19.90"), null, categoria)
        );
        Pedido pedidoPublico = pedidoService.criar(new PedidoRequest(
            null,
            "Cliente Público",
            "publico-consulta@fatiaprime.test",
            "(11) 98888-7777",
            "Rua da Consulta, 20",
            null,
            java.util.List.of(new ItemPedidoRequest(produto.getId(), 1))
        ));

        mockMvc.perform(get("/api/pedidos/consulta").param("telefone", "11988887777"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(pedidoPublico.getCodigo()))
            .andExpect(jsonPath("$[0].nomeCliente").value("Cliente Público"))
            .andExpect(jsonPath("$[0].telefone").value("(11) 98888-7777"));
    }

    @Test
    void codigoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", "FP-INEXISTENTE"))
            .andExpect(status().isNotFound());
    }

    @Test
    void telefoneSemPedidosRetorna404() throws Exception {
        mockMvc.perform(get("/api/pedidos/consulta").param("telefone", "11999999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void consultaSemFiltroRetorna400() throws Exception {
        mockMvc.perform(get("/api/pedidos/consulta"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void consultaComDoisFiltrosRetorna400() throws Exception {
        mockMvc.perform(get("/api/pedidos/consulta")
                .param("codigo", pedido.getCodigo())
                .param("telefone", "61987654321"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listagemAdministrativaContinuaProtegida() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
            .andExpect(status().isUnauthorized());
    }
}