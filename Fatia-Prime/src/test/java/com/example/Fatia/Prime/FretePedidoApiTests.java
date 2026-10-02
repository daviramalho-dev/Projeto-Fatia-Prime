package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.frete.loja-lat=0.0", "app.frete.loja-lng=0.0", "app.frete.raio-km=30"})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FretePedidoApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

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

    @Autowired
    private FreteProperties freteProperties;

    @MockitoBean
    private CepGeocoder cepGeocoder;

    private Produto saborEconomico;
    private Produto saborPremium;
    private OpcaoPizza borda;
    private OpcaoPizza adicional;
    private Usuario admin;

    @BeforeEach
    void prepararDados() {
        when(cepGeocoder.geocodificar(anyString()))
            .thenAnswer(invocation -> FreteTestCoordinates.paraCep(invocation.getArgument(0)));
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
        opcaoPizzaRepository.deleteAll();
        categoriaRepository.deleteAll();

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
            .andExpect(jsonPath("$.regiao").value("0 a 10 km"))
            .andExpect(jsonPath("$.distanciaKm").value(5.0))
            .andExpect(jsonPath("$.valorFrete").value(5.00));
        mockMvc.perform(get("/api/frete/consulta").param("cep", "99990-100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cep").value("99990100"))
            .andExpect(jsonPath("$.regiao").value("Acima de 10 ate 20 km"))
            .andExpect(jsonPath("$.distanciaKm").value(15.0))
            .andExpect(jsonPath("$.valorFrete").value(8.00));
        mockMvc.perform(get("/api/frete/consulta").param("cep", "99990299"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.regiao").value("Acima de 20 ate 30 km"))
            .andExpect(jsonPath("$.distanciaKm").value(25.0))
            .andExpect(jsonPath("$.valorFrete").value(12.00));
    }

    @Test
    void cotacaoRejeitaCepVazioIncompletoMalformadoEAcimaDoRaio() throws Exception {
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
            .andExpect(jsonPath("$.message").value(
                "Ainda não entregamos nessa região. Em breve abriremos novas unidades mais perto de você!"
            ));
    }

    @Test
    void falhaDeGeocodificacaoRetorna503SemMensagemDeForaDaArea() throws Exception {
        when(cepGeocoder.geocodificar("72500107"))
            .thenThrow(new CepGeocoder.GeocodificacaoIndisponivelException());

        mockMvc.perform(get("/api/frete/consulta").param("cep", "72500107"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.message")
                .value("Não foi possível calcular a entrega no momento. Tente novamente."));
    }

    @Test
    void pedidoCalculaSubtotalFreteETotalNoServidorEPreservaFreteJaCobrado() throws Exception {
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

        BigDecimal valorAnterior = freteProperties.getFaixas().get(1).getValor();
        try {
            freteProperties.getFaixas().get(1).setValor(new BigDecimal("99.00"));
            mockMvc.perform(get("/api/pedidos/consulta").param("codigo", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subtotal").value(80.00))
                .andExpect(jsonPath("$[0].frete").value(8.00))
                .andExpect(jsonPath("$[0].valorTotal").value(88.00));
        } finally {
            freteProperties.getFaixas().get(1).setValor(valorAnterior);
        }
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
                .content(pedidoJson("99990400", """
                    {"produtoId":%d,"quantidade":1}
                    """.formatted(saborEconomico.getId()), "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(
                "Ainda não entregamos nessa região. Em breve abriremos novas unidades mais perto de você!"
            ));

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
    void respostasAdminEConsultaMantemCepFreteEPersonalizacao() throws Exception {
        String corpo = pedidoJson("99990-100", """
            {"produtoId":%d,"quantidade":1,"tipoPizza":"MEIO_A_MEIO","segundoProdutoId":%d,
             "bordaId":%d,"adicionalIds":[%d]}
            """.formatted(saborEconomico.getId(), saborPremium.getId(), borda.getId(), adicional.getId()), "");
        String resposta = mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        String codigo = resposta.replaceAll(".*\\\"codigo\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", codigo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].subtotal").value(61.00))
            .andExpect(jsonPath("$[0].frete").value(8.00))
            .andExpect(jsonPath("$[0].itens[0].nomeSegundoProduto").value("Frango com Catupiry"));
        mockMvc.perform(get("/api/admin/pedidos").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].cep").value("99990100"))
            .andExpect(jsonPath("$[0].subtotal").value(61.00))
            .andExpect(jsonPath("$[0].frete").value(8.00))
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
              "clienteNome":"Cliente BL51",
              "clienteEmail":"cliente-bl51@fatiaprime.test",
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