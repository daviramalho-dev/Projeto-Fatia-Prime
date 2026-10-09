package com.example.Fatia.Prime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
    "app.frete.loja-lat=0.0",
    "app.frete.loja-lng=0.0"
})
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
@Testcontainers
class PostgreSqlPersistenceTests {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.hikari.data-source-properties.sslmode", () -> "disable");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private OpcaoPizzaRepository opcaoPizzaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CepGeocoder cepGeocoder;

    @BeforeEach
    void configureGeocoder() {
        when(cepGeocoder.geocodificarOpcoes(anyString()))
            .thenReturn(List.of(new CepGeocoder.Coordenadas(0.0, 0.0, "teste", false)));
    }

    @Test
    void persistsSeedAndRerunningItDoesNotDuplicateCatalogRows() {
        int categoriesBefore = JdbcTestUtils.countRowsInTable(jdbcTemplate, "categorias");
        int productsBefore = JdbcTestUtils.countRowsInTable(jdbcTemplate, "produtos");
        int optionsBefore = JdbcTestUtils.countRowsInTable(jdbcTemplate, "opcoes_pizza");

        new ResourceDatabasePopulator(new ClassPathResource("data.sql")).execute(
            jdbcTemplate.getDataSource()
        );

        assertThat(categoriesBefore).isGreaterThanOrEqualTo(6);
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Clássicas")).isTrue();
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Carnes")).isTrue();
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Frango")).isTrue();
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Queijos")).isTrue();
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Doces")).isTrue();
        assertThat(categoriaRepository.existsByNomeIgnoreCase("Bebidas")).isTrue();
        assertThat(productsBefore).isGreaterThan(30);
        assertThat(optionsBefore).isGreaterThan(0);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "categorias")).isEqualTo(categoriesBefore);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "produtos")).isEqualTo(productsBefore);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "opcoes_pizza")).isEqualTo(optionsBefore);
    }

    @Test
    void persistsCheckoutItemsAndStatusForAdminOnPostgres() throws Exception {
        String unique = UUID.randomUUID().toString();
        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("Teste PostgreSQL " + unique));
        Produto produto = produtoRepository.saveAndFlush(new Produto(
            "Pizza PostgreSQL " + unique,
            "Produto usado no teste de integração PostgreSQL",
            new BigDecimal("52.90"),
            null,
            categoria
        ));
        OpcaoPizza adicional = opcaoPizzaRepository
            .findByAtivoTrueAndTipoProdutoOrderByTipoAscNomeAsc(TipoProdutoPizza.SALGADA)
            .stream()
            .filter(opcao -> opcao.getTipo() == TipoOpcaoPizza.ADICIONAL)
            .findFirst()
            .orElseThrow();
        BigDecimal subtotalEsperado = new BigDecimal("52.90").add(adicional.getPrecoAdicional());
        BigDecimal totalEsperado = subtotalEsperado.add(new BigDecimal("5.00"));

        String requestBody = """
            {
              "clienteNome": "Cliente PostgreSQL",
              "clienteTelefone": "61999998888",
              "cep": "99990000",
              "endereco": "Rua PostgreSQL, 1",
              "observacoes": "Teste de persistência",
              "itens": [{"produtoId": %d, "quantidade": 1, "adicionalIds": [%d]}]
            }
            """.formatted(produto.getId(), adicional.getId());

        MvcResult checkout = mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cep").value("99990000"))
            .andExpect(jsonPath("$.itens.length()").value(1))
            .andExpect(jsonPath("$.itens[0].adicionais[0].nome").value(adicional.getNome()))
            .andExpect(jsonPath("$.subtotal").value(subtotalEsperado.doubleValue()))
            .andExpect(jsonPath("$.frete").value(5.00))
            .andExpect(jsonPath("$.valorTotal").value(totalEsperado.doubleValue()))
            .andReturn();

        String body = checkout.getResponse().getContentAsString();
        String code = body.replaceAll(".*\\\"codigo\\\":\\\"([^\\\"]+)\\\".*", "$1");
        Pedido pedido = pedidoRepository.findByCodigoForConsulta(code).orElseThrow();
        assertThat(pedido.getCep()).isEqualTo("99990000");
        assertThat(pedido.getItens()).hasSize(1);
        assertThat(pedido.getItens().getFirst().getProduto().getNome()).isEqualTo(produto.getNome());
        assertThat(pedido.getItens().getFirst().getPrecoUnitario()).isEqualByComparingTo(subtotalEsperado);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from item_pedido_adicionais where item_pedido_id = ?",
            Integer.class,
            pedido.getItens().getFirst().getId()
        )).isEqualTo(1);

        String adminEmail = "admin-" + unique + "@fatiaprime.test";
        String adminPassword = UUID.randomUUID().toString();
        usuarioRepository.saveAndFlush(new Usuario(
            "Administrador PostgreSQL",
            adminEmail,
            passwordEncoder.encode(adminPassword),
            UsuarioRole.ADMIN
        ));

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", adminEmail)
                .param("senha", adminPassword))
            .andExpect(status().isNoContent())
            .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        String customerEmail = "cliente-" + unique + "@fatiaprime.test";
        String customerPassword = UUID.randomUUID().toString();
        mockMvc.perform(post("/api/usuarios")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Cliente PostgreSQL","email":"%s","senha":"%s"}
                    """.formatted(customerEmail, customerPassword)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(customerEmail))
            .andExpect(jsonPath("$.senhaHash").doesNotExist())
            .andExpect(jsonPath("$.senha_hash").doesNotExist());

        Usuario customer = usuarioRepository.findByEmailIgnoreCase(customerEmail).orElseThrow();
        assertThat(customer.getRole()).isEqualTo(UsuarioRole.USER);
        assertThat(passwordEncoder.matches(customerPassword, customer.getSenhaHash())).isTrue();

        mockMvc.perform(get("/api/admin/pedidos")
                .session(session)
                .param("codigo", code))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigo").value(code))
            .andExpect(jsonPath("$[0].itens.length()").value(1));

        mockMvc.perform(patch("/api/admin/pedidos/{id}/status", pedido.getId())
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Pedido em andamento\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Pedido em andamento"));

        MvcResult customerLogin = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .param("email", customerEmail)
                .param("senha", customerPassword))
            .andExpect(status().isNoContent())
            .andReturn();
        MockHttpSession customerSession = (MockHttpSession) customerLogin.getRequest().getSession(false);
        assertThat(customerSession).isNotNull();
        mockMvc.perform(get("/api/admin/pedidos").session(customerSession))
            .andExpect(status().isForbidden());

        assertThat(pedidoRepository.findById(pedido.getId()).orElseThrow().getStatus())
            .isEqualTo("Pedido em andamento");
        assertThat(categoriaRepository.findById(categoria.getId())).isPresent();
        assertThat(produtoRepository.findById(produto.getId())).isPresent();
        assertThat(usuarioRepository.findByEmailIgnoreCase(adminEmail)).isPresent();
    }
}
