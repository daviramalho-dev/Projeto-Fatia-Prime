package com.example.Fatia.Prime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
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
class AdminPedidoApiTests {

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

    private Usuario admin;
    private Pedido pedido;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();

        admin = new Usuario("João da Silva", "admin-api@fatiaprime.test", passwordEncoder.encode("senha-123"), Perfil.ADMIN);
        admin.setTelefone("(11) 98765-4321");
        admin = usuarioRepository.saveAndFlush(admin);

        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("Carnes"));
        Produto produto = produtoRepository.saveAndFlush(new Produto(
            "Calabresa Prime", "Calabresa e mozzarella", new BigDecimal("49.90"), null, categoria));

        pedido = new Pedido();
        pedido.setUsuario(admin);
        pedido.setCodigo("FP-TESTE-001");
        pedido.setStatus("Pedido recebido");
        pedido.setObservacoes("Sem cebola");
        pedido.setValorTotal(new BigDecimal("99.80"));
        pedido.setItens(List.of(new ItemPedido(produto, 2, new BigDecimal("49.90"))));
        pedido.getItens().get(0).setPedido(pedido);
        pedido = pedidoRepository.saveAndFlush(pedido);
    }

    @Test
    void listaPedidosAutenticado() throws Exception {
        mockMvc.perform(get("/api/admin/pedidos").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value("FP-TESTE-001"))
            .andExpect(jsonPath("$[0].status").value("Pedido recebido"))
            .andExpect(jsonPath("$[0].itens[0].quantidade").value(2))
            .andExpect(jsonPath("$[0].total").value(99.80));
    }

    @Test
    void listaPedidosSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/admin/pedidos"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void detalhaPedidoExistenteSemExporSenha() throws Exception {
        mockMvc.perform(get("/api/admin/pedidos/{id}", pedido.getId()).session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("FP-TESTE-001"))
            .andExpect(jsonPath("$.nomeCliente").value("João da Silva"))
            .andExpect(jsonPath("$.telefone").value("(11) 98765-4321"))
            .andExpect(jsonPath("$.email").value("admin-api@fatiaprime.test"))
            .andExpect(jsonPath("$.senha_hash").doesNotExist())
            .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void pedidoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/admin/pedidos/999999").session(adminSession()))
            .andExpect(status().isNotFound());
    }

    @Test
    void combinaFiltrosDeStatusTelefoneNomeECodigo() throws Exception {
        mockMvc.perform(get("/api/admin/pedidos")
                .param("status", "Pedido recebido")
                .param("telefone", "11987654321")
                .param("nome", "joão")
                .param("codigo", "teste-001")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(pedido.getId()));
    }

    @Test
    void filtrosIndividuaisFuncionam() throws Exception {
        mockMvc.perform(get("/api/admin/pedidos").param("status", "Pedido recebido").session(adminSession()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/admin/pedidos").param("telefone", "98765").session(adminSession()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/admin/pedidos").param("nome", "silva").session(adminSession()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/admin/pedidos").param("codigo", "FP-TESTE").session(adminSession()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void statusInvalidoRetorna400SemAlterarPedido() throws Exception {
        mockMvc.perform(patch("/api/admin/pedidos/{id}/status", pedido.getId())
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Cancelado\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Status inválido")));
    }

    @Test
    void alteraStatusAutenticadoEContinuaPersistido() throws Exception {
        mockMvc.perform(patch("/api/admin/pedidos/{id}/status", pedido.getId())
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Pedido em andamento\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Pedido em andamento"));

        org.junit.jupiter.api.Assertions.assertEquals(
            "Pedido em andamento", pedidoRepository.findById(pedido.getId()).orElseThrow().getStatus());
    }

    @Test
    void alteraStatusSemAutenticacaoRetorna401() throws Exception {
        mockMvc.perform(patch("/api/admin/pedidos/{id}/status", pedido.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Pedido em andamento\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void pedidoInexistenteAoAlterarStatusRetorna404() throws Exception {
        mockMvc.perform(patch("/api/admin/pedidos/999999/status")
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Pedido em andamento\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void naoPermitePularEtapasDeStatus() throws Exception {
        mockMvc.perform(patch("/api/admin/pedidos/{id}/status", pedido.getId())
                .session(adminSession()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Pedido concluído\"}"))
            .andExpect(status().isConflict());
    }

    private MockHttpSession adminSession() throws Exception {
        var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                .with(csrf())
                .param("email", "admin-api@fatiaprime.test")
                .param("senha", "senha-123"))
            .andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }
}
