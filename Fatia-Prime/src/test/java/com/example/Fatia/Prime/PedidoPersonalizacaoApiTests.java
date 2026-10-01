package com.example.Fatia.Prime;

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

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PedidoPersonalizacaoApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private OpcaoPizzaRepository opcaoPizzaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Produto saborEconomico;
    private Produto saborPremium;
    private Produto saborInativo;
    private Produto saborDoce;
    private OpcaoPizza bordaCatupiry;
    private OpcaoPizza adicionalQueijo;
    private OpcaoPizza opcaoDoce;
    private OpcaoPizza adicionalMorango;
    private Usuario admin;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
        opcaoPizzaRepository.deleteAll();
        categoriaRepository.deleteAll();

        Categoria salgados = categoriaRepository.saveAndFlush(new Categoria("Salgadas"));
        Categoria doces = categoriaRepository.saveAndFlush(new Categoria("Doces"));
        saborEconomico = produtoRepository.saveAndFlush(produto("Calabresa Prime", "40.00", salgados, TipoProdutoPizza.SALGADA, true));
        saborPremium = produtoRepository.saveAndFlush(produto("Frango com Catupiry", "50.00", salgados, TipoProdutoPizza.SALGADA, true));
        saborInativo = produtoRepository.saveAndFlush(produto("Sabor indisponível", "60.00", salgados, TipoProdutoPizza.SALGADA, false));
        saborDoce = produtoRepository.saveAndFlush(produto("Chocolate com Morango", "55.00", doces, TipoProdutoPizza.DOCE, true));

        bordaCatupiry = opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Catupiry", TipoOpcaoPizza.BORDA, TipoProdutoPizza.SALGADA, new BigDecimal("7.00")));
        adicionalQueijo = opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Queijo extra", TipoOpcaoPizza.ADICIONAL, TipoProdutoPizza.SALGADA, new BigDecimal("4.00")));
        opcaoDoce = opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Chocolate", TipoOpcaoPizza.BORDA, TipoProdutoPizza.DOCE, new BigDecimal("6.00")));
        adicionalMorango = opcaoPizzaRepository.saveAndFlush(new OpcaoPizza(
            "Morango extra", TipoOpcaoPizza.ADICIONAL, TipoProdutoPizza.DOCE, new BigDecimal("5.00")));
        admin = usuarioRepository.saveAndFlush(new Usuario(
            "Admin BL48", "admin-bl48@fatiaprime.test", passwordEncoder.encode("senha-123"), UsuarioRole.ADMIN));
    }

    @Test
    void pedidoInteiroRecalculaPrecoEIgnoraPrecoEnviado() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"tipoPizza":"INTEIRA","precoUnitario":0,"valorTotal":0}
            """.formatted(saborEconomico.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.subtotal").value(40.00))
            .andExpect(jsonPath("$.frete").value(5.00))
            .andExpect(jsonPath("$.valorTotal").value(45.00))
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(40.00))
            .andExpect(jsonPath("$.itens[0].tipoPizza").value("INTEIRA"))
            .andExpect(jsonPath("$.itens[0].nomeProduto").value("Calabresa Prime"));
    }

    @Test
    void meioAMeioUsaMaiorPrecoESomaBordaAdicionaisEQuantidade() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":2,"tipoPizza":"MEIO_A_MEIO","segundoProdutoId":%d,
             "bordaId":%d,"adicionalIds":[%d],"precoUnitario":1,"valorTotal":1}
            """.formatted(saborEconomico.getId(), saborPremium.getId(), bordaCatupiry.getId(), adicionalQueijo.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.subtotal").value(122.00))
            .andExpect(jsonPath("$.frete").value(5.00))
            .andExpect(jsonPath("$.valorTotal").value(127.00))
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(61.00))
            .andExpect(jsonPath("$.itens[0].tipoPizza").value("MEIO_A_MEIO"))
            .andExpect(jsonPath("$.itens[0].nomeProduto").value("Calabresa Prime"))
            .andExpect(jsonPath("$.itens[0].nomeSegundoProduto").value("Frango com Catupiry"))
            .andExpect(jsonPath("$.itens[0].borda").value("Catupiry"))
            .andExpect(jsonPath("$.itens[0].precoBorda").value(7.00))
            .andExpect(jsonPath("$.itens[0].adicionais[0].nome").value("Queijo extra"))
            .andExpect(jsonPath("$.itens[0].adicionais[0].precoAdicional").value(4.00));
    }

    @Test
    void meioAMeioUsaOMaiorPrecoMesmoQuandoEleEoPrimeiroSabor() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"tipoPizza":"MEIO_A_MEIO","segundoProdutoId":%d}
            """.formatted(saborPremium.getId(), saborEconomico.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(50.00))
            .andExpect(jsonPath("$.subtotal").value(50.00))
            .andExpect(jsonPath("$.frete").value(5.00))
            .andExpect(jsonPath("$.valorTotal").value(55.00));
    }

    @Test
    void meioAMeioSemSegundoSaborRetorna400() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"tipoPizza":"MEIO_A_MEIO"}
            """.formatted(saborEconomico.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void saborInexistenteRetorna404() throws Exception {
        pedidoComItem("{\"produtoId\":999999,\"quantidade\":1}")
            .andExpect(status().isNotFound());
    }

    @Test
    void saborInativoRetorna400() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1}
            """.formatted(saborInativo.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void meioAMeioNaoMisturaSaboresDocesESalgados() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"tipoPizza":"MEIO_A_MEIO","segundoProdutoId":%d}
            """.formatted(saborEconomico.getId(), saborDoce.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void pizzaInteiraRejeitaSegundoSabor() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"tipoPizza":"INTEIRA","segundoProdutoId":%d}
            """.formatted(saborEconomico.getId(), saborPremium.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void bordaDeOutroTipoRetorna400() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"bordaId":%d}
            """.formatted(saborEconomico.getId(), opcaoDoce.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void bordaInexistenteOuDesativadaRetorna400() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"bordaId":999999}
            """.formatted(saborEconomico.getId()))
            .andExpect(status().isBadRequest());

        bordaCatupiry.setAtivo(false);
        opcaoPizzaRepository.saveAndFlush(bordaCatupiry);
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"bordaId":%d}
            """.formatted(saborEconomico.getId(), bordaCatupiry.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void adicionalInvalidoRetorna400() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"adicionalIds":[%d]}
            """.formatted(saborEconomico.getId(), bordaCatupiry.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void adicionalInexistenteOuDesativadoRetorna400() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"adicionalIds":[999999]}
            """.formatted(saborEconomico.getId()))
            .andExpect(status().isBadRequest());

        adicionalQueijo.setAtivo(false);
        opcaoPizzaRepository.saveAndFlush(adicionalQueijo);
        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"adicionalIds":[%d]}
            """.formatted(saborEconomico.getId(), adicionalQueijo.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void quantidadeInvalidaRetorna400() throws Exception {
        pedidoComItem("""
            {"produtoId":%d,"quantidade":51}
            """.formatted(saborEconomico.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void consultaPublicaEAdministracaoMantemDetalhesPersonalizados() throws Exception {
        String resposta = pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"tipoPizza":"MEIO_A_MEIO","segundoProdutoId":%d,
             "bordaId":%d,"adicionalIds":[%d]}
            """.formatted(saborEconomico.getId(), saborPremium.getId(), bordaCatupiry.getId(), adicionalQueijo.getId()))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        String codigo = resposta.replaceAll(".*\\\"codigo\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/pedidos/consulta").param("codigo", codigo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].subtotal").value(61.00))
            .andExpect(jsonPath("$[0].frete").value(5.00))
            .andExpect(jsonPath("$[0].itens[0].nomeSegundoProduto").value("Frango com Catupiry"))
            .andExpect(jsonPath("$[0].itens[0].borda").value("Catupiry"))
            .andExpect(jsonPath("$[0].itens[0].adicionais[0].nome").value("Queijo extra"));

        mockMvc.perform(get("/api/admin/pedidos").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].subtotal").value(61.00))
            .andExpect(jsonPath("$[0].frete").value(5.00))
            .andExpect(jsonPath("$[0].itens[0].nomeProduto").value("Calabresa Prime"))
            .andExpect(jsonPath("$[0].itens[0].nomeSegundoProduto").value("Frango com Catupiry"))
            .andExpect(jsonPath("$[0].itens[0].borda").value("Catupiry"))
            .andExpect(jsonPath("$[0].itens[0].adicionais[0].nome").value("Queijo extra"));
    }

    @Test
    void produtoDoceAceitaBordaEAdicionalDoces() throws Exception {
        mockMvc.perform(get("/api/opcoes-pizza").param("tipoProduto", "DOCE"))
            .andExpect(status().isOk());

        pedidoComItem("""
            {"produtoId":%d,"quantidade":1,"bordaId":%d,"adicionalIds":[%d]}
            """.formatted(saborDoce.getId(), opcaoDoce.getId(), adicionalMorango.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.subtotal").value(66.00))
            .andExpect(jsonPath("$.frete").value(5.00))
            .andExpect(jsonPath("$.valorTotal").value(71.00))
            .andExpect(jsonPath("$.itens[0].borda").value("Chocolate"))
            .andExpect(jsonPath("$.itens[0].adicionais[0].nome").value("Morango extra"));
    }

    private Produto produto(
        String nome,
        String preco,
        Categoria categoria,
        TipoProdutoPizza tipo,
        boolean ativo
    ) {
        Produto produto = new Produto(nome, "Descrição", new BigDecimal(preco), null, categoria);
        produto.setTipo(tipo);
        produto.setAtivo(ativo);
        return produto;
    }

    private org.springframework.test.web.servlet.ResultActions pedidoComItem(String item) throws Exception {
        String corpo = """
            {
              "clienteNome":"Cliente BL48",
              "clienteEmail":"cliente-bl48@fatiaprime.test",
              "clienteTelefone":"61999998888",
              "cep":"99990000",
              "endereco":"Rua da Pizza, 10",
              "itens":[%s]
            }
            """.formatted(item);
        return mockMvc.perform(post("/api/pedidos")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo));
    }

    private MockHttpSession adminSession() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", admin.getEmail())
                .param("senha", "senha-123"))
            .andReturn()
            .getRequest()
            .getSession();
    }
}
