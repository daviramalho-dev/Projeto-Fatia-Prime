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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FretePedidoApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private FaixaFreteRepository faixaFreteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private OpcaoPizzaRepository opcaoPizzaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Produto saborEconomico;
    private Produto saborPremium;
    private OpcaoPizza borda;
    private OpcaoPizza adicional;
    private FaixaFrete regiao1;
    private FaixaFrete regiao2;
    private FaixaFrete regiao3;
    private Usuario admin;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
        opcaoPizzaRepository.deleteAll();
        categoriaRepository.deleteAll();
        faixaFreteRepository.deleteAll();

        regiao1 = faixaFreteRepository.saveAndFlush(new FaixaFrete(
            "Teste Região 1", "99990000", "99990099", new BigDecimal("5.00"), true));
        regiao2 = faixaFreteRepository.saveAndFlush(new FaixaFrete(
            "Teste Região 2", "99990100", "99990199", new BigDecimal("8.00"), true));
        regiao3 = faixaFreteRepository.saveAndFlush(new FaixaFrete(
            "Teste Região 3", "99990200", "99990299", new BigDecimal("12.00"), true));
        faixaFreteRepository.saveAndFlush(new FaixaFrete(
            "Teste Região inativa", "99990300", "99990399", new BigDecimal("1.00"), false));

        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("Salgadas"));
        saborEconomico = produtoRepository.saveAndFlush(produto("Calabresa", "40.00", categoria));
        saborPremium = produtoRepository.saveAndFlush(produto("Frango com Catupiry", "50.00", categoria));
        borda = opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Catupiry", TipoOpcaoPizza.BORDA, TipoProdutoPizza.SALGADA, new BigDecimal("7.00")));
        adicional = opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Queijo extra", TipoOpcaoPizza.ADICIONAL, TipoProdutoPizza.SALGADA, new BigDecimal("4.00")));
        admin = usuarioRepository.saveAndFlush(new Usuario(
            "Admin BL49", "admin-bl49@fatiaprime.test", passwordEncoder.encode("senha-123"), UsuarioRole.ADMIN));
    }

    @Test
    void consultaAsTresFaixasAceitaCepComOuSemHifenENormalizaResposta() throws Exception {
        mockMvc.perform(get("/api/frete/consulta").param("cep", "99990000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cep").value("99990000"))
            .andExpect(jsonPath("$.regiao").value("Teste Região 1"))
            .andExpect(jsonPath("$.valorFrete").value(5.00));
        mockMvc.perform(get("/api/frete/consulta").param("cep", "99990-100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cep").value("99990100"))
            .andExpect(jsonPath("$.valorFrete").value(8.00));
        mockMvc.perform(get("/api/frete/consulta").param("cep", "99990299"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valorFrete").value(12.00));
    }

    @Test
    void cotacaoRejeitaCepVazioIncompletoMalformadoEFaixaInativa() throws Exception {
        mockMvc.perform(get("/api/frete/consulta"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/frete/consulta").param("cep", "99990-0"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/frete/consulta").param("cep", "abc99990000"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/frete/consulta").param("cep", "999900001"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/frete/consulta").param("cep", "99990300"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Infelizmente, ainda não entregamos nessa região."));
        mockMvc.perform(get("/api/frete/consulta").param("cep", "99990400"))
            .andExpect(status().isNotFound());
    }

    @Test
    void pedidoCalculaSubtotalFreteETotalNoServidorIgnorandoValoresDoCliente() throws Exception {
        String corpo = pedidoJson("99990-100", """
            {"produtoId":%d,"quantidade":2,"precoUnitario":0}
            """.formatted(saborEconomico.getId()), """
            ,"frete":0,"subtotal":1,"valorTotal":1
            """);

        String resposta = mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cep").value("99990100"))
            .andExpect(jsonPath("$.subtotal").value(80.00))
            .andExpect(jsonPath("$.frete").value(8.00))
            .andExpect(jsonPath("$.valorTotal").value(88.00))
            .andReturn().getResponse().getContentAsString();
        String codigo = resposta.replaceAll(".*\\\"codigo\\\":\\\"([^\\\"]+)\\\".*", "$1");
        Pedido pedido = pedidoRepository.findAll().get(0);
        assertEquals("99990100", pedido.getCep());
        assertEquals(new BigDecimal("8.00"), pedido.getValorFrete());
        assertEquals(new BigDecimal("88.00"), pedido.getValorTotal());

        regiao2 = faixaFreteRepository.findById(regiao2.getId()).orElseThrow();
        faixaFreteRepository.delete(regiao2);
        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", codigo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].cep").doesNotExist())
            .andExpect(jsonPath("$[0].subtotal").value(80.00))
            .andExpect(jsonPath("$[0].frete").value(8.00))
            .andExpect(jsonPath("$[0].valorTotal").value(88.00));
    }

    @Test
    void pedidoForaDaAreaOuSemCepOuEnderecoNaoEGravado() throws Exception {
        mockMvc.perform(post("/api/pedidos")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(pedidoJson("", """
                {"produtoId":%d,"quantidade":1}
                """.formatted(saborEconomico.getId()), "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Informe seu CEP."));

        mockMvc.perform(post("/api/pedidos")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(pedidoJson("99990-0", """
                {"produtoId":%d,"quantidade":1}
                """.formatted(saborEconomico.getId()), "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Informe um CEP válido."));

            mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson("999900001", """
                    {"produtoId":%d,"quantidade":1}
                    """.formatted(saborEconomico.getId()), "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Informe um CEP válido."));

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson("99990400", """
                    {"produtoId":%d,"quantidade":1}
                    """.formatted(saborEconomico.getId()), "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Infelizmente, ainda não entregamos nessa região."));

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"clienteNome":"Cliente","clienteEmail":"cliente@exemplo.test","clienteTelefone":"61999998888",
                     "cep":"99990000","itens":[{"produtoId":%d,"quantidade":1}]}
                    """.formatted(saborEconomico.getId())))
            .andExpect(status().isBadRequest());
        assertEquals(0, pedidoRepository.count());
    }

    @Test
    void meioAMeioBl48SomaPersonalizacaoAntesDoFrete() throws Exception {
        String corpo = pedidoJson("99990200", """
            {"produtoId":%d,"quantidade":2,"tipoPizza":"MEIO_A_MEIO","segundoProdutoId":%d,
             "bordaId":%d,"adicionalIds":[%d],"subtotal":0,"frete":0,"valorTotal":0}
            """.formatted(saborEconomico.getId(), saborPremium.getId(), borda.getId(), adicional.getId()), "");

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.subtotal").value(122.00))
            .andExpect(jsonPath("$.frete").value(12.00))
            .andExpect(jsonPath("$.valorTotal").value(134.00))
            .andExpect(jsonPath("$.itens[0].tipoPizza").value("MEIO_A_MEIO"))
            .andExpect(jsonPath("$.itens[0].nomeSegundoProduto").value("Frango com Catupiry"))
            .andExpect(jsonPath("$.itens[0].borda").value("Catupiry"))
            .andExpect(jsonPath("$.itens[0].adicionais[0].nome").value("Queijo extra"));
    }

    @Test
    void respostaAdministrativaExplicaSubtotalFreteETotal() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson("99990000", """
                    {"produtoId":%d,"quantidade":1}
                    """.formatted(saborEconomico.getId()), "")))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/pedidos").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].cep").value("99990000"))
            .andExpect(jsonPath("$[0].subtotal").value(40.00))
            .andExpect(jsonPath("$[0].frete").value(5.00))
            .andExpect(jsonPath("$[0].total").value(45.00));
    }

    @Test
    void painelAdminMantemEnderecoValoresEFidelidadeDaPersonalizacaoBl48() throws Exception {
        String corpo = pedidoJson("99990-100", """
            {"produtoId":%d,"quantidade":1,"tipoPizza":"MEIO_A_MEIO","segundoProdutoId":%d,
             "bordaId":%d,"adicionalIds":[%d]}
            """.formatted(saborEconomico.getId(), saborPremium.getId(), borda.getId(), adicional.getId()), "");

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/pedidos").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].cep").value("99990100"))
            .andExpect(jsonPath("$[0].endereco").value("Rua de teste, 10 · Centro · Cidade - UF · CEP 99990-100"))
            .andExpect(jsonPath("$[0].subtotal").value(61.00))
            .andExpect(jsonPath("$[0].frete").value(8.00))
            .andExpect(jsonPath("$[0].total").value(69.00))
            .andExpect(jsonPath("$[0].itens[0].tipoPizza").value("MEIO_A_MEIO"))
            .andExpect(jsonPath("$[0].itens[0].nomeSegundoProduto").value("Frango com Catupiry"))
            .andExpect(jsonPath("$[0].itens[0].borda").value("Catupiry"))
            .andExpect(jsonPath("$[0].itens[0].adicionais[0].nome").value("Queijo extra"));
    }

    private Produto produto(String nome, String preco, Categoria categoria) {
        return produtoRepository.saveAndFlush(new Produto(
            nome, "Descrição", new BigDecimal(preco), null, categoria));
    }

    private String pedidoJson(String cep, String item, String valoresManipulados) {
        return """
            {
              "clienteNome":"Cliente BL49",
              "clienteEmail":"cliente-bl49@fatiaprime.test",
              "clienteTelefone":"61999998888",
              "cep":"%s",
              "endereco":"Rua de teste, 10 · Centro · Cidade - UF · CEP %s",
              "itens":[%s]%s
            }
            """.formatted(cep, cep, item, valoresManipulados);
    }

    private MockHttpSession adminSession() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", admin.getEmail())
                .param("senha", "senha-123"))
            .andReturn().getRequest().getSession();
    }
}
