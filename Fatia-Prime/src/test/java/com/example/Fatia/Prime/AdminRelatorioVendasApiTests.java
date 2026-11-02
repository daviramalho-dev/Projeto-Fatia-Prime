package com.example.Fatia.Prime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminRelatorioVendasApiTests {

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

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();

        admin = new Usuario("Admin Relatório", "admin-relatorio@fatiaprime.test", passwordEncoder.encode("senha-123"), UsuarioRole.ADMIN);
        admin = usuarioRepository.saveAndFlush(admin);

        Categoria cat = categoriaRepository.saveAndFlush(new Categoria("Clássicas"));
        Produto prod = produtoRepository.saveAndFlush(new Produto("Margherita", "Molho e queijo", new BigDecimal("50.00"), null, cat));

        // Pedido gravado em UTC, correspondente a 23:30 em Brasília
        Pedido pedido1 = new Pedido();
        pedido1.setUsuario(admin);
        pedido1.setCodigo("FP-REL-001");
        pedido1.setStatus("Pedido concluído");
        pedido1.setCep("70000000");
        pedido1.setValorTotal(new BigDecimal("50.00"));
        org.springframework.test.util.ReflectionTestUtils.setField(pedido1, "dataCriacao", LocalDateTime.of(2026, 10, 2, 2, 30, 0));
        ItemPedido item1 = new ItemPedido(prod, 1, new BigDecimal("50.00"));
        item1.setPedido(pedido1);
        pedido1.setItens(List.of(item1));
        pedidoRepository.saveAndFlush(pedido1);

        // Pedido cancelado
        Pedido pedidoCancelado = new Pedido();
        pedidoCancelado.setUsuario(admin);
        pedidoCancelado.setCodigo("FP-REL-CANC");
        pedidoCancelado.setStatus(" Cancelado ");
        pedidoCancelado.setCep("70000000");
        pedidoCancelado.setValorTotal(new BigDecimal("80.00"));
        org.springframework.test.util.ReflectionTestUtils.setField(pedidoCancelado, "dataCriacao", LocalDateTime.of(2026, 10, 1, 15, 0, 0));
        pedidoRepository.saveAndFlush(pedidoCancelado);
    }

    @Test
    void exportaXlsxComDataEValorNumericos() throws Exception {
        var response = mockMvc.perform(get("/api/admin/relatorios/vendas/xlsx")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andReturn();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(response.getResponse().getContentAsByteArray()))) {
            Sheet sheet = workbook.getSheet("Vendas por dia");
            Cell data = sheet.getRow(1).getCell(0);
            Cell valor = sheet.getRow(1).getCell(2);
            assertThat(data.getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(data.getCellStyle().getDataFormatString()).contains("dd/MM/yyyy HH:mm");
            assertThat(valor.getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(valor.getNumericCellValue()).isEqualTo(50.0);
            assertThat(valor.getCellStyle().getDataFormatString()).contains("R$");
            assertThat(workbook.getSheet("Vendas por categoria").getRow(1).getCell(0).getStringCellValue()).isEqualTo("Clássicas");
        }
    }

    @Test
    void periodoRetornaDatasValidas() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/vendas/periodo").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.primeiroPedido").value("2026-10-01"))
            .andExpect(jsonPath("$.hoje").exists());
    }

    @Test
    void relatorioFiltraFusoETotaisCorretamente() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumo.faturamento").value(50.00))
            .andExpect(jsonPath("$.resumo.totalPedidos").value(1))
            .andExpect(jsonPath("$.resumo.ticketMedio").value(50.00))
            .andExpect(jsonPath("$.resumo.cancelados").value(1))
            .andExpect(jsonPath("$.porCategoria[0].categoria").value("Clássicas"))
            .andExpect(jsonPath("$.porCategoria[0].faturamento").value(50.00));
    }

    @Test
    void relatorioFiltraMeiaMeiaEPeriodoVazio() throws Exception {
        // Pedido meia a meio e pedido na madrugada (00h10)
        Categoria cat2 = categoriaRepository.saveAndFlush(new Categoria("Especiais"));
        Produto prod1 = produtoRepository.saveAndFlush(new Produto("Calabresa", "Linguiça", new BigDecimal("40.00"), null, cat2));
        produtoRepository.saveAndFlush(new Produto("Frango", "Frango c/ catupiry", new BigDecimal("45.00"), null, cat2));

        Pedido pedidoMeia = new Pedido();
        pedidoMeia.setUsuario(admin);
        pedidoMeia.setCodigo("FP-REL-MEIA");
        pedidoMeia.setStatus("Pedido concluído");
        pedidoMeia.setCep("70000000");
        pedidoMeia.setValorTotal(new BigDecimal("45.00"));
        org.springframework.test.util.ReflectionTestUtils.setField(pedidoMeia, "dataCriacao", LocalDateTime.of(2026, 10, 2, 3, 10, 0));

        ItemPedido itemMeia = new ItemPedido(prod1, 1, new BigDecimal("45.00"));
        itemMeia.setTipoPizza(TipoPizza.MEIO_A_MEIO);
        itemMeia.setNomeProdutoSnapshot("Calabresa");
        itemMeia.setNomeSegundoProdutoSnapshot("Frango");
        itemMeia.setPedido(pedidoMeia);
        pedidoMeia.setItens(List.of(itemMeia));
        pedidoRepository.saveAndFlush(pedidoMeia);

        // Teste período sem pedidos
        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-09-01")
                .param("fim", "2026-09-05")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumo.faturamento").value(0.00))
            .andExpect(jsonPath("$.resumo.totalPedidos").value(0))
            .andExpect(jsonPath("$.porDia[0].faturamento").value(0.00));

        // Teste dia 2026-10-02 com pedido à 00h10
        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-02")
                .param("fim", "2026-10-02")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumo.faturamento").value(45.00))
            .andExpect(jsonPath("$.resumo.totalPedidos").value(1))
            .andExpect(jsonPath("$.porCategoria[0].categoria").value("Meio a meio"));
    }

    @Test
    void naoContemDadosPessoais() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumo.nomeCliente").doesNotExist())
            .andExpect(jsonPath("$.resumo.telefone").doesNotExist())
            .andExpect(jsonPath("$.porDia[0].endereco").doesNotExist());
    }

    @Test
    void validacoesDeParametros() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-05")
                .param("fim", "2026-10-01")
                .session(adminSession()))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2024-01-01")
                .param("fim", "2025-06-01")
                .session(adminSession()))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("366 dias")));
    }

    @Test
    void b1_pedidoComBordaAdicionalEMolhoCoincideComResumoAdmin() throws Exception {
        pedidoRepository.deleteAll();
        Produto prod = produtoRepository.findAll().get(0);

        // Pedido gravado de verdade com borda (7.00), adicional (5.00) e molho (3.00)
        // precoBase (50.00) + borda (7.00) + adicional (5.00) + molho (3.00) = 65.00
        BigDecimal precoBase = prod.getPreco();
        BigDecimal precoBorda = new BigDecimal("7.00");
        BigDecimal precoAdicional = new BigDecimal("5.00");
        BigDecimal precoMolho = new BigDecimal("3.00");
        BigDecimal precoUnitario = precoBase.add(precoBorda).add(precoAdicional).add(precoMolho);

        Pedido pedidoComExtras = new Pedido();
        pedidoComExtras.setUsuario(admin);
        pedidoComExtras.setCodigo("FP-REL-B1");
        pedidoComExtras.setStatus("Pedido concluído");
        pedidoComExtras.setCep("70000000");
        pedidoComExtras.setValorTotal(precoUnitario);
        org.springframework.test.util.ReflectionTestUtils.setField(pedidoComExtras, "dataCriacao", LocalDateTime.of(2026, 10, 1, 18, 0, 0));

        ItemPedido itemExtras = new ItemPedido(prod, 1, precoUnitario);
        itemExtras.setBordaNome("Catupiry");
        itemExtras.setBordaPreco(precoBorda);
        itemExtras.setAdicionais(List.of(
            new ItemPedidoAdicional(1L, "Bacon", precoAdicional, TipoOpcaoPizza.ADICIONAL),
            new ItemPedidoAdicional(2L, "Barbecue", precoMolho, TipoOpcaoPizza.MOLHO)
        ));
        itemExtras.setPedido(pedidoComExtras);
        pedidoComExtras.setItens(List.of(itemExtras));
        pedidoRepository.saveAndFlush(pedidoComExtras);

        // Resumo existente do admin (/api/admin/pedidos/dashboard)
        var dashboardRes = mockMvc.perform(get("/api/admin/pedidos/dashboard").session(adminSession()))
            .andExpect(status().isOk())
            .andReturn();
        String dashJson = dashboardRes.getResponse().getContentAsString();
        BigDecimal valorResumoAdmin = new BigDecimal(
            com.jayway.jsonpath.JsonPath.read(dashJson, "$.valorTotalPedidos").toString());

        // Relatório de vendas (/api/admin/relatorios/vendas)
        var relatorioRes = mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andReturn();
        String relJson = relatorioRes.getResponse().getContentAsString();
        BigDecimal valorRelatorio = new BigDecimal(
            com.jayway.jsonpath.JsonPath.read(relJson, "$.resumo.faturamento").toString());

        System.out.println("=== B1: COMPARAÇÃO RESUMO ADMIN x RELATÓRIO ===");
        System.out.println("Valor total no Resumo Existente (admin/pedidos/dashboard): R$ " + valorResumoAdmin);
        System.out.println("Valor total no Relatório de Vendas (relatorios/vendas):   R$ " + valorRelatorio);

        assertThat(valorRelatorio).isEqualByComparingTo(valorResumoAdmin);
    }

    @Test
    void b2_somaDasLinhasPorCategoriaIgualAoFaturamentoDoPeriodo() throws Exception {
        Produto prod = produtoRepository.findAll().get(0);
        Categoria catEspeciais = categoriaRepository.saveAndFlush(new Categoria("Especiais"));
        Produto prod2 = produtoRepository.saveAndFlush(new Produto("Calabresa", "Linguiça", new BigDecimal("40.00"), null, catEspeciais));
        Produto prod3 = produtoRepository.saveAndFlush(new Produto("Frango", "Frango", new BigDecimal("46.00"), null, catEspeciais));

        // 1. Pedido com extras: borda (7.00) + adicional (5.00) + molho (3.00) = 15.00 extras + 50.00 base = 65.00
        BigDecimal precoUnitExtras = new BigDecimal("65.00");
        Pedido pedido1 = new Pedido();
        pedido1.setUsuario(admin);
        pedido1.setCodigo("FP-B2-EXTRAS");
        pedido1.setStatus("Pedido concluído");
        pedido1.setCep("70000000");
        pedido1.setValorTotal(precoUnitExtras);
        org.springframework.test.util.ReflectionTestUtils.setField(pedido1, "dataCriacao", LocalDateTime.of(2026, 10, 1, 14, 0, 0));
        ItemPedido item1 = new ItemPedido(prod, 1, precoUnitExtras);
        item1.setBordaNome("Catupiry");
        item1.setBordaPreco(new BigDecimal("7.00"));
        item1.setAdicionais(List.of(
            new ItemPedidoAdicional(1L, "Bacon", new BigDecimal("5.00"), TipoOpcaoPizza.ADICIONAL),
            new ItemPedidoAdicional(2L, "Barbecue", new BigDecimal("3.00"), TipoOpcaoPizza.MOLHO)
        ));
        item1.setPedido(pedido1);
        pedido1.setItens(List.of(item1));
        pedidoRepository.saveAndFlush(pedido1);

        // 2. Pedido meio a meio: 46.00
        Pedido pedidoMeia = new Pedido();
        pedidoMeia.setUsuario(admin);
        pedidoMeia.setCodigo("FP-B2-MEIA");
        pedidoMeia.setStatus("Pedido concluído");
        pedidoMeia.setCep("70000000");
        pedidoMeia.setValorTotal(new BigDecimal("46.00"));
        org.springframework.test.util.ReflectionTestUtils.setField(pedidoMeia, "dataCriacao", LocalDateTime.of(2026, 10, 1, 16, 0, 0));
        ItemPedido itemMeia = new ItemPedido(prod2, 1, new BigDecimal("46.00"));
        itemMeia.setTipoPizza(TipoPizza.MEIO_A_MEIO);
        itemMeia.setNomeProdutoSnapshot("Calabresa");
        itemMeia.setNomeSegundoProdutoSnapshot("Frango");
        itemMeia.setSegundoProduto(prod3);
        itemMeia.setPedido(pedidoMeia);
        pedidoMeia.setItens(List.of(itemMeia));
        pedidoRepository.saveAndFlush(pedidoMeia);

        // Faturamento esperado do dia: 50.00 (setup) + 65.00 (extras) + 46.00 (meia) = 161.00
        var relatorioRes = mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andReturn();
        String json = relatorioRes.getResponse().getContentAsString();

        BigDecimal faturamentoTotal = new BigDecimal(
            com.jayway.jsonpath.JsonPath.read(json, "$.resumo.faturamento").toString());
        List<java.util.Map<String, Object>> categorias =
            com.jayway.jsonpath.JsonPath.read(json, "$.porCategoria");

        BigDecimal somaCategorias = BigDecimal.ZERO;
        System.out.println("=== B2: SOMA POR CATEGORIA x FATURAMENTO DO PERÍODO ===");
        boolean achouMeioAMeio = false;
        boolean achouPersonalizacao = false;
        for (var cat : categorias) {
            String nome = (String) cat.get("categoria");
            BigDecimal fat = new BigDecimal(cat.get("faturamento").toString());
            System.out.println("Categoria: " + nome + " -> R$ " + fat);
            somaCategorias = somaCategorias.add(fat);
            if ("Meio a meio".equals(nome)) achouMeioAMeio = true;
            if ("Personalização".equals(nome)) achouPersonalizacao = true;
        }
        System.out.println("Soma das categorias: R$ " + somaCategorias);
        System.out.println("Faturamento total:   R$ " + faturamentoTotal);

        assertThat(achouMeioAMeio).isTrue();
        assertThat(achouPersonalizacao).isTrue();
        assertThat(somaCategorias).isEqualByComparingTo(faturamentoTotal);
    }

    @Test
    void b3_dataFuturaRejeitadaSessaoAdmin200EAnonimoRejeitado() throws Exception {
        // 1. Data futura rejeitada (400)
        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2099-01-01")
                .param("fim", "2099-01-02")
                .session(adminSession()))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Datas não podem ser futuras")));

        // 2. Sessão de admin recebe 200 nos dois endpoints
        mockMvc.perform(get("/api/admin/relatorios/vendas/periodo").session(adminSession()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01")
                .session(adminSession()))
            .andExpect(status().isOk());

        // 3. Anônimo rejeitado nos dois endpoints (401)
        mockMvc.perform(get("/api/admin/relatorios/vendas/periodo"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void normalizaStatusComAcentoEspacosEMaiusculas() {
        assertThat(AdminRelatorioVendasController.statusCancelado("Cancelado")).isTrue();
        assertThat(AdminRelatorioVendasController.statusCancelado(" cancelado ")).isTrue();
        assertThat(AdminRelatorioVendasController.statusCancelado("CANCELADO")).isTrue();
        assertThat(AdminRelatorioVendasController.statusConcluido("Pedido concluído")).isTrue();
        assertThat(AdminRelatorioVendasController.statusConcluido("Pedido concluido")).isTrue();
        assertThat(AdminRelatorioVendasController.statusConcluido("PEDIDO CONCLUÍDO")).isTrue();
    }

    @Test
    void filtraSomenteEntreguesComStatusNormalizado() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01")
                .param("considerar", "entregues")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumo.totalPedidos").value(1));
    }

    @Test
    void acessoAnonimoOuComumRejeitado() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01")
                .param("fim", "2026-10-01"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void p11_freteApareceComoEntregaESomaIgualFaturamento() throws Exception {
        // Pedido com frete de R$ 6,00: valorTotal = 50,00 (produto) + 6,00 (frete) = 56,00
        pedidoRepository.deleteAll();
        Produto prod = produtoRepository.findAll().get(0);

        Pedido pedidoComFrete = new Pedido();
        pedidoComFrete.setUsuario(admin);
        pedidoComFrete.setCodigo("FP-REL-FRETE");
        pedidoComFrete.setStatus("Pedido concluído");
        pedidoComFrete.setCep("70000000");
        pedidoComFrete.setValorFrete(new BigDecimal("6.00"));
        pedidoComFrete.setValorTotal(new BigDecimal("56.00"));
        org.springframework.test.util.ReflectionTestUtils.setField(pedidoComFrete, "dataCriacao", LocalDateTime.of(2026, 10, 1, 15, 0, 0));
        ItemPedido item = new ItemPedido(prod, 1, new BigDecimal("50.00"));
        item.setPedido(pedidoComFrete);
        pedidoComFrete.setItens(List.of(item));
        pedidoRepository.saveAndFlush(pedidoComFrete);

        // Resumo admin (soma todos os pedidos, inclui frete)
        var dashRes = mockMvc.perform(get("/api/admin/pedidos/dashboard").session(adminSession()))
            .andExpect(status().isOk()).andReturn();
        BigDecimal valorResumoAdmin = new BigDecimal(
            com.jayway.jsonpath.JsonPath.read(dashRes.getResponse().getContentAsString(), "$.valorTotalPedidos").toString());

        // Relatório de vendas
        var relRes = mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01").param("fim", "2026-10-01").session(adminSession()))
            .andExpect(status().isOk()).andReturn();
        String relJson = relRes.getResponse().getContentAsString();
        BigDecimal faturamentoRelatorio = new BigDecimal(
            com.jayway.jsonpath.JsonPath.read(relJson, "$.resumo.faturamento").toString());

        List<java.util.Map<String, Object>> categorias = com.jayway.jsonpath.JsonPath.read(relJson, "$.porCategoria");
        BigDecimal somaCategorias = BigDecimal.ZERO;
        boolean achouEntrega = false;
        BigDecimal valorEntrega = BigDecimal.ZERO;
        for (var cat : categorias) {
            BigDecimal fat = new BigDecimal(cat.get("faturamento").toString());
            somaCategorias = somaCategorias.add(fat);
            if ("Entrega".equals(cat.get("categoria"))) {
                achouEntrega = true;
                valorEntrega = fat;
            }
        }

        System.out.println("=== P1.1: FRETE ===");
        System.out.println("Resumo existente (dashboard):  R$ " + valorResumoAdmin);
        System.out.println("Faturamento do relatório:      R$ " + faturamentoRelatorio);
        System.out.println("Soma por categoria:            R$ " + somaCategorias);
        System.out.println("Linha Entrega:                 R$ " + valorEntrega);

        assertThat(faturamentoRelatorio).isEqualByComparingTo(valorResumoAdmin);
        assertThat(achouEntrega).as("Deve existir linha 'Entrega' na tabela por categoria").isTrue();
        assertThat(valorEntrega).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(((Number) categorias.stream().filter(cat -> "Entrega".equals(cat.get("categoria"))).findFirst().orElseThrow().get("quantidade")).longValue()).isEqualTo(1L);
        assertThat(somaCategorias).isEqualByComparingTo(faturamentoRelatorio);
    }

    @Test
    void p12_precoBaseNegativoAtribuiItemTodoACategoria() throws Exception {
        // Dado antigo onde precoUnitario < soma dos extras → baseUnit negativo.
        // Nesse caso o item inteiro deve ir para a categoria do produto, sem linha "Personalização".
        pedidoRepository.deleteAll();
        Produto prod = produtoRepository.findAll().get(0);

        // precoUnitario=5.00, borda=7.00 → base=5-7=-2 (negativo)
        BigDecimal precoUnitario = new BigDecimal("5.00");
        Pedido pedido = new Pedido();
        pedido.setUsuario(admin);
        pedido.setCodigo("FP-REL-NEG");
        pedido.setStatus("Pedido concluído");
        pedido.setCep("70000000");
        pedido.setValorTotal(precoUnitario);
        org.springframework.test.util.ReflectionTestUtils.setField(pedido, "dataCriacao", LocalDateTime.of(2026, 10, 1, 15, 0, 0));
        ItemPedido item = new ItemPedido(prod, 1, precoUnitario);
        item.setBordaNome("Catupiry");
        item.setBordaPreco(new BigDecimal("7.00"));
        item.setPedido(pedido);
        pedido.setItens(List.of(item));
        pedidoRepository.saveAndFlush(pedido);

        var relRes = mockMvc.perform(get("/api/admin/relatorios/vendas")
                .param("inicio", "2026-10-01").param("fim", "2026-10-01").session(adminSession()))
            .andExpect(status().isOk()).andReturn();
        String relJson = relRes.getResponse().getContentAsString();

        BigDecimal faturamento = new BigDecimal(
            com.jayway.jsonpath.JsonPath.read(relJson, "$.resumo.faturamento").toString());
        List<java.util.Map<String, Object>> categorias = com.jayway.jsonpath.JsonPath.read(relJson, "$.porCategoria");

        System.out.println("=== P1.2: BASE NEGATIVA ===");
        boolean achouPersonalizacao = false;
        BigDecimal soma = BigDecimal.ZERO;
        for (var cat : categorias) {
            System.out.println(cat.get("categoria") + " -> R$ " + cat.get("faturamento"));
            soma = soma.add(new BigDecimal(cat.get("faturamento").toString()));
            if ("Personalização".equals(cat.get("categoria"))) achouPersonalizacao = true;
        }
        System.out.println("Faturamento: R$ " + faturamento + " | Soma categorias: R$ " + soma);

        assertThat(achouPersonalizacao).as("Não deve haver 'Personalização' quando preço base é negativo").isFalse();
        assertThat(soma).isEqualByComparingTo(faturamento);
    }

    @Test
    void p13_periodoRetornaHojeEmBrasilia() throws Exception {
        // /periodo deve retornar "hoje" no fuso America/Sao_Paulo, nunca UTC
        var res = mockMvc.perform(get("/api/admin/relatorios/vendas/periodo").session(adminSession()))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();
        String hojeApi = com.jayway.jsonpath.JsonPath.read(json, "$.hoje");

        java.time.LocalDate hojeEsperado = java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo"));
        System.out.println("=== P1.3: FUSO ===");
        System.out.println("Hoje em Brasilia: " + hojeEsperado);
        System.out.println("Hoje na API:      " + hojeApi);

        assertThat(hojeApi).isEqualTo(hojeEsperado.toString());
    }

    private MockHttpSession adminSession() throws Exception {
        var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                .with(csrf())
                .param("email", "admin-relatorio@fatiaprime.test")
                .param("senha", "senha-123"))
            .andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }
}
