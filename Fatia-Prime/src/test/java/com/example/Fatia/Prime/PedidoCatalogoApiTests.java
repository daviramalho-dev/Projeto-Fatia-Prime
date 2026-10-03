package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.frete.loja-lat=0.0", "app.frete.loja-lng=0.0"})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PedidoCatalogoApiTests {

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

    @MockitoBean
    private CepGeocoder cepGeocoder;

    private Produto saborPrincipal;
    private Produto segundoSabor;
    private Produto bebida;
    private Produto produtoInativo;
    private OpcaoPizza borda;
    private OpcaoPizza adicional;
    private OpcaoPizza molho;
    private OpcaoPizza adicionalInativo;
    private OpcaoPizza molhoInativo;
    private Usuario admin;

    @BeforeEach
    void prepararDados() {
        when(cepGeocoder.geocodificar(anyString()))
            .thenAnswer(invocation -> FreteTestCoordinates.paraCep(invocation.getArgument(0)));
        when(cepGeocoder.geocodificarOpcoes(anyString()))
            .thenAnswer(invocation -> List.of(cepGeocoder.geocodificar(invocation.getArgument(0))));
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
        opcaoPizzaRepository.deleteAll();
        categoriaRepository.deleteAll();
        faixaFreteRepository.deleteAll();

        faixaFreteRepository.saveAndFlush(new FaixaFrete(
            "Região BL50", "99990100", "99990199", new BigDecimal("8.00"), true));
        Categoria salgados = categoriaRepository.saveAndFlush(new Categoria("Salgadas BL50"));
        Categoria bebidas = categoriaRepository.saveAndFlush(new Categoria("Bebidas BL50"));
        saborPrincipal = produto("Calabresa BL50", "50.00", salgados, TipoProdutoPizza.SALGADA, true);
        segundoSabor = produto("Frango BL50", "40.00", salgados, TipoProdutoPizza.SALGADA, true);
        bebida = produto("Bebida BL50", "12.50", bebidas, TipoProdutoPizza.BEBIDA, true);
        produtoInativo = produto("Produto inativo BL50", "15.00", bebidas, TipoProdutoPizza.BEBIDA, false);

        borda = opcao("Borda BL50", TipoOpcaoPizza.BORDA, "7.00", true);
        adicional = opcao("Queijo BL50", TipoOpcaoPizza.ADICIONAL, "4.00", true);
        molho = opcao("Molho BL50", TipoOpcaoPizza.MOLHO, "2.50", true);
        adicionalInativo = opcao("Adicional inativo BL50", TipoOpcaoPizza.ADICIONAL, "3.00", false);
        molhoInativo = opcao("Molho inativo BL50", TipoOpcaoPizza.MOLHO, "1.00", false);
        admin = usuarioRepository.saveAndFlush(new Usuario(
            "Admin BL50", "admin-bl50@fatiaprime.test", passwordEncoder.encode("senha-123"), UsuarioRole.ADMIN));
    }

    @Test
    void bebidaNoCarrinhoUsaPrecoDoBancoEQuantidadeSemPersonalizacao() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson("""
                    {"produtoId":%d,"quantidade":2,"tipoPizza":"INTEIRA","precoUnitario":0,
                     "adicionalIds":[],"molhoIds":[],"subtotal":0,"frete":0,"valorTotal":0}
                    """.formatted(bebida.getId()))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.itens[0].tipoProduto").value("BEBIDA"))
            .andExpect(jsonPath("$.itens[0].nomeProduto").value("Bebida BL50"))
            .andExpect(jsonPath("$.itens[0].quantidade").value(2))
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(12.50))
            .andExpect(jsonPath("$.itens[0].subtotal").value(25.00))
            .andExpect(jsonPath("$.subtotal").value(25.00))
            .andExpect(jsonPath("$.frete").value(8.00))
            .andExpect(jsonPath("$.valorTotal").value(33.00))
            .andExpect(jsonPath("$.itens[0].adicionais.length()").value(0))
            .andExpect(jsonPath("$.itens[0].molhos.length()").value(0));
    }

    @Test
    void pedidoComPizzaMeioAMeioBordaAdicionalMolhoEBebidaMantemSnapshotsNasConsultas() throws Exception {
        String itens = """
            {"produtoId":%d,"quantidade":1,"tipoPizza":"MEIO_A_MEIO","segundoProdutoId":%d,
             "bordaId":%d,"adicionalIds":[%d],"adicionais":[{"opcaoId":%d,"precoAdicional":0}],
             "molhoIds":[%d],"molhos":[{"opcaoId":%d,"precoAdicional":0}],
             "precoUnitario":0,"precoAdicional":0,"subtotal":0},
            {"produtoId":%d,"quantidade":2,"tipoPizza":"INTEIRA","precoUnitario":0,"molhoIds":[]}
            """.formatted(
                saborPrincipal.getId(), segundoSabor.getId(), borda.getId(), adicional.getId(), adicional.getId(),
                molho.getId(), molho.getId(), bebida.getId());

        String resposta = mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson(itens)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.subtotal").value(88.50))
            .andExpect(jsonPath("$.frete").value(8.00))
            .andExpect(jsonPath("$.valorTotal").value(96.50))
            .andExpect(jsonPath("$.itens[0].tipoPizza").value("MEIO_A_MEIO"))
            .andExpect(jsonPath("$.itens[0].nomeSegundoProduto").value("Frango BL50"))
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(63.50))
            .andExpect(jsonPath("$.itens[0].adicionais[0].nome").value("Queijo BL50"))
            .andExpect(jsonPath("$.itens[0].adicionais[0].precoAdicional").value(4.00))
            .andExpect(jsonPath("$.itens[0].molhos[0].nome").value("Molho BL50"))
            .andExpect(jsonPath("$.itens[0].molhos[0].tipo").value("MOLHO"))
            .andExpect(jsonPath("$.itens[1].tipoProduto").value("BEBIDA"))
            .andExpect(jsonPath("$.itens[1].quantidade").value(2))
            .andReturn().getResponse().getContentAsString();
        String codigo = resposta.replaceAll(".*\\\"codigo\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", codigo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].subtotal").value(88.50))
            .andExpect(jsonPath("$[0].frete").value(8.00))
            .andExpect(jsonPath("$[0].valorTotal").value(96.50))
            .andExpect(jsonPath("$[0].itens[0].molhos[0].nome").value("Molho BL50"))
            .andExpect(jsonPath("$[0].itens[1].nomeProduto").value("Bebida BL50"))
            .andExpect(jsonPath("$[0].clienteEmail").doesNotExist());

        mockMvc.perform(get("/api/admin/pedidos").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].subtotal").value(88.50))
            .andExpect(jsonPath("$[0].frete").value(8.00))
            .andExpect(jsonPath("$[0].total").value(96.50))
            .andExpect(jsonPath("$[0].itens[0].adicionais[0].nome").value("Queijo BL50"))
            .andExpect(jsonPath("$[0].itens[0].molhos[0].nome").value("Molho BL50"))
            .andExpect(jsonPath("$[0].itens[1].tipoProduto").value("BEBIDA"));

        bebida.setNome("Bebida renomeada");
        bebida.setTipo(TipoProdutoPizza.SALGADA);
        produtoRepository.saveAndFlush(bebida);
        molho.setNome("Molho renomeado");
        molho.setPrecoAdicional(new BigDecimal("9.99"));
        opcaoPizzaRepository.saveAndFlush(molho);
        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", codigo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].itens[0].molhos[0].nome").value("Molho BL50"))
            .andExpect(jsonPath("$[0].itens[0].molhos[0].precoAdicional").value(2.50))
            .andExpect(jsonPath("$[0].itens[1].tipoProduto").value("BEBIDA"))
            .andExpect(jsonPath("$[0].itens[1].nomeProduto").value("Bebida BL50"));
    }

            @Test
            void pedidoComPizzaSimplesEBebidaSomaOsDoisProdutos() throws Exception {
            mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson("""
                    {"produtoId":%d,"quantidade":1,"tipoPizza":"INTEIRA"},
                    {"produtoId":%d,"quantidade":1,"tipoPizza":"INTEIRA"}
                    """.formatted(saborPrincipal.getId(), bebida.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].tipoProduto").value("SALGADA"))
                .andExpect(jsonPath("$.itens[1].tipoProduto").value("BEBIDA"))
                .andExpect(jsonPath("$.subtotal").value(62.50))
                .andExpect(jsonPath("$.frete").value(8.00))
                .andExpect(jsonPath("$.valorTotal").value(70.50));
            }

    @Test
    void rejeitaProdutosEOpcoesInativosEPersonalizacaoEmBebidas() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1}
            """.formatted(produtoInativo.getId()))
            .andExpect(status().isBadRequest());

        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"adicionalIds":[%d]}
            """.formatted(saborPrincipal.getId(), adicionalInativo.getId()))
            .andExpect(status().isBadRequest());

        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"molhoIds":[%d]}
            """.formatted(saborPrincipal.getId(), molhoInativo.getId()))
            .andExpect(status().isBadRequest());

        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"molhoIds":[999999]}
            """.formatted(saborPrincipal.getId()))
            .andExpect(status().isBadRequest());

        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"molhoIds":[%d]}
            """.formatted(bebida.getId(), molho.getId()))
            .andExpect(status().isBadRequest());

        assertEquals(0, pedidoRepository.count());
    }

    @Test
    void bebidaEListadaNoCatalogoEProdutoBebidaPodeSerCriadoPeloAdmin() throws Exception {
        mockMvc.perform(get("/api/produtos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].tipo").value(org.hamcrest.Matchers.hasItem("BEBIDA")));

        mockMvc.perform(post("/api/admin/produtos")
                .session(adminSession())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Bebida criada pelo admin","descricao":"Demonstração","preco":10.00,
                     "categoriaId":%d,"tipo":"BEBIDA","ativo":true}
                    """.formatted(bebida.getCategoria().getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("BEBIDA"))
            .andExpect(jsonPath("$.ativo").value(true));

        mockMvc.perform(get("/api/opcoes-pizza").param("tipoProduto", "SALGADA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].tipo").value(org.hamcrest.Matchers.hasItem("MOLHO")));

        mockMvc.perform(post("/api/admin/opcoes-pizza")
                .session(adminSession())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Molho admin BL50","tipo":"MOLHO","tipoProduto":"SALGADA",
                     "precoAdicional":1.25,"ativo":true}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("MOLHO"))
            .andExpect(jsonPath("$.precoAdicional").value(1.25));
    }

    private Produto produto(String nome, String preco, Categoria categoria, TipoProdutoPizza tipo, boolean ativo) {
        Produto produto = new Produto(nome, "Descrição de teste", new BigDecimal(preco), null, categoria);
        produto.setTipo(tipo);
        produto.setAtivo(ativo);
        return produtoRepository.saveAndFlush(produto);
    }

    private OpcaoPizza opcao(String nome, TipoOpcaoPizza tipo, String preco, boolean ativo) {
        OpcaoPizza opcao = new OpcaoPizza(nome, tipo, TipoProdutoPizza.SALGADA, new BigDecimal(preco));
        opcao.setAtivo(ativo);
        return opcaoPizzaRepository.saveAndFlush(opcao);
    }

    private String pedidoJson(String itens) {
        return """
            {
              "clienteNome":"Cliente BL50",
              "clienteEmail":"cliente-bl50@fatiaprime.test",
              "clienteTelefone":"61999998888",
              "cep":"99990100",
              "endereco":"Rua BL50, 10 · Centro · Cidade - UF · CEP 99990-100",
              "subtotal":0,
              "frete":0,
              "valorTotal":0,
              "itens":[%s]
            }
            """.formatted(itens);
    }

    private org.springframework.test.web.servlet.ResultActions pedidoComItem(String item) throws Exception {
        return mockMvc.perform(post("/api/pedidos")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(pedidoJson(item)));
    }

    private MockHttpSession adminSession() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", admin.getEmail())
                .param("senha", "senha-123"))
            .andReturn().getRequest().getSession();
    }
}