package com.example.Fatia.Prime;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.text.Normalizer;
import java.util.*;

@RestController
@RequestMapping("/api/admin/relatorios/vendas")
public class AdminRelatorioVendasController {

    private static final ZoneId ZONA_BRASILIA = ZoneId.of("America/Sao_Paulo");
    private final PedidoRepository pedidoRepository;

    public AdminRelatorioVendasController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/periodo")
    @Transactional(readOnly = true)
    public RelatorioVendasPeriodoResponse periodo() {
        LocalDate hoje = LocalDate.now(ZONA_BRASILIA);
        List<Pedido> pedidos = pedidoRepository.findAllForAdmin();
        LocalDate primeiroPedido = pedidos.stream()
            .filter(p -> p.getDataCriacao() != null)
            .map(p -> dataEmBrasilia(p.getDataCriacao()).toLocalDate())
            .min(LocalDate::compareTo)
            .orElse(null);
        return new RelatorioVendasPeriodoResponse(primeiroPedido, hoje);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public RelatorioVendasResponse relatorio(
        @RequestParam(required = false) String inicio,
        @RequestParam(required = false) String fim,
        @RequestParam(required = false, defaultValue = "todos") String considerar
    ) {
        LocalDate dataInicio = validarData(inicio, "Data inicial inválida");
        LocalDate dataFim = validarData(fim, "Data final inválida");

        if (dataInicio.isAfter(dataFim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data inicial não pode ser posterior à data final");
        }
        if (ChronoUnit.DAYS.between(dataInicio, dataFim) > 366) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O intervalo máximo permitido é de 366 dias");
        }

        LocalDate hoje = LocalDate.now(ZONA_BRASILIA);
        if (dataInicio.isAfter(hoje) || dataFim.isAfter(hoje)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datas não podem ser futuras");
        }

        List<Pedido> todosPedidos = pedidoRepository.findAllForAdmin();

        List<Pedido> pedidosNoPeriodo = todosPedidos.stream()
            .filter(p -> p.getDataCriacao() != null)
            .filter(p -> {
                LocalDateTime dtBrasilia = dataEmBrasilia(p.getDataCriacao());
                LocalDate dataLocal = dtBrasilia.toLocalDate();
                return !dataLocal.isBefore(dataInicio) && !dataLocal.isAfter(dataFim);
            })
            .toList();

        String filtroConsiderar = normalizarStatus(considerar);
        boolean apenasEntregues = "entregues".equals(filtroConsiderar) || "concluidos".equals(filtroConsiderar);

        long cancelados = pedidosNoPeriodo.stream()
            .filter(p -> statusCancelado(p.getStatus()))
            .count();

        List<Pedido> pedidosValidos = pedidosNoPeriodo.stream()
            .filter(p -> !statusCancelado(p.getStatus()))
            .filter(p -> !apenasEntregues || statusConcluido(p.getStatus()))
            .toList();

        BigDecimal faturamentoTotal = BigDecimal.ZERO;
        BigDecimal freteTotal = BigDecimal.ZERO;
        long pedidosComFrete = 0;
        Map<String, BigDecimal> faturamentoPorDiaMap = new TreeMap<>();
        Map<String, Long> pedidosPorDiaMap = new TreeMap<>();
        Map<String, CategoriaStats> categoriaStatsMap = new LinkedHashMap<>();
        Map<String, ProdutoStats> maisVendidosMap = new HashMap<>();
        Map<Integer, HorarioStats> porHorarioMap = new TreeMap<>();

        for (int h = 0; h < 24; h++) {
            porHorarioMap.put(h, new HorarioStats(0, BigDecimal.ZERO));
        }

        for (LocalDate d = dataInicio; !d.isAfter(dataFim); d = d.plusDays(1)) {
            String diaStr = d.toString();
            faturamentoPorDiaMap.put(diaStr, BigDecimal.ZERO);
            pedidosPorDiaMap.put(diaStr, 0L);
        }

        for (Pedido p : pedidosValidos) {
            BigDecimal valorPedido = p.getValorTotal() != null ? p.getValorTotal() : BigDecimal.ZERO;
            BigDecimal valorFretePedido = p.getValorFrete();
            faturamentoTotal = faturamentoTotal.add(valorPedido);
            freteTotal = freteTotal.add(valorFretePedido);
            if (valorFretePedido.compareTo(BigDecimal.ZERO) > 0) pedidosComFrete++;

            LocalDate dataLocal = dataEmBrasilia(p.getDataCriacao()).toLocalDate();
            String diaKey = dataLocal.toString();
            faturamentoPorDiaMap.put(diaKey, faturamentoPorDiaMap.getOrDefault(diaKey, BigDecimal.ZERO).add(valorPedido));
            pedidosPorDiaMap.put(diaKey, pedidosPorDiaMap.getOrDefault(diaKey, 0L) + 1L);

            int hora = dataEmBrasilia(p.getDataCriacao()).getHour();
            HorarioStats hs = porHorarioMap.get(hora);
            porHorarioMap.put(hora, new HorarioStats(hs.pedidos() + 1, hs.faturamento().add(valorPedido)));

            if (p.getItens() != null) {
                for (ItemPedido item : p.getItens()) {
                    if (item == null) continue;
                    int qtd = item.getQuantidade() != null ? item.getQuantidade() : 1;
                    BigDecimal precoUnit = item.getPrecoUnitario() != null ? item.getPrecoUnitario() : BigDecimal.ZERO;
                    BigDecimal precoBorda = item.getBordaPreco() != null ? item.getBordaPreco() : BigDecimal.ZERO;
                    BigDecimal precoExtrasUnit = precoBorda;
                    int countExtras = precoBorda.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0;
                    if (item.getAdicionais() != null) {
                        for (ItemPedidoAdicional add : item.getAdicionais()) {
                            if (add != null && add.getPrecoAdicional() != null) {
                                precoExtrasUnit = precoExtrasUnit.add(add.getPrecoAdicional());
                                countExtras++;
                            }
                        }
                    }

                    BigDecimal extrasTotal = precoExtrasUnit.multiply(BigDecimal.valueOf(qtd));
                    BigDecimal baseUnit = precoUnit.subtract(precoExtrasUnit);
                    // Se preço base ficou negativo (dado antigo), trata item inteiro como da categoria do produto
                    boolean extrasNegativo = baseUnit.compareTo(BigDecimal.ZERO) < 0;
                    BigDecimal baseTotal = extrasNegativo
                        ? precoUnit.multiply(BigDecimal.valueOf(qtd))
                        : baseUnit.multiply(BigDecimal.valueOf(qtd));

                    String catNome = "Outros";
                    if (item.getProduto() != null && item.getProduto().getCategoria() != null) {
                        catNome = item.getProduto().getCategoria().getNome();
                    } else if (item.getTipoProdutoSnapshot() != null) {
                        catNome = item.getTipoProdutoSnapshot().name();
                    }

                    if (item.getTipoPizza() == TipoPizza.MEIO_A_MEIO) {
                        catNome = "Meio a meio";
                    }

                    adicionarCategoria(categoriaStatsMap, catNome, qtd, baseTotal);
                    if (!extrasNegativo && extrasTotal.compareTo(BigDecimal.ZERO) > 0) {
                        adicionarCategoria(categoriaStatsMap, "Personalização", countExtras * qtd, extrasTotal);
                    }

                    String nomeProd = item.getNomeProdutoSnapshot() != null ? item.getNomeProdutoSnapshot() : (item.getProduto() != null ? item.getProduto().getNome() : "Produto");
                    if (item.getTipoPizza() == TipoPizza.MEIO_A_MEIO && item.getNomeSegundoProdutoSnapshot() != null) {
                        nomeProd = nomeProd + " / " + item.getNomeSegundoProdutoSnapshot();
                    }
                    BigDecimal itemTotal = precoUnit.multiply(BigDecimal.valueOf(qtd));
                    adicionarMaisVendido(maisVendidosMap, nomeProd, catNome, qtd, itemTotal);

                }
            }
        }

        // Linha "Entrega": soma o frete dos pedidos válidos que realmente cobraram entrega.
        // Garante que a soma das categorias == faturamentoTotal (que já inclui o frete).
        if (freteTotal.compareTo(BigDecimal.ZERO) > 0) {
            adicionarCategoria(categoriaStatsMap, "Entrega", pedidosComFrete, freteTotal);
        }

        long totalPedidosValidos = pedidosValidos.size();
        BigDecimal ticketMedio = totalPedidosValidos > 0
            ? faturamentoTotal.divide(BigDecimal.valueOf(totalPedidosValidos), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        List<RelatorioVendasResponse.VendaPorDia> porDia = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : faturamentoPorDiaMap.entrySet()) {
            String dia = entry.getKey();
            porDia.add(new RelatorioVendasResponse.VendaPorDia(dia, entry.getValue(), pedidosPorDiaMap.getOrDefault(dia, 0L)));
        }

        List<RelatorioVendasResponse.VendaPorCategoria> porCategoria = new ArrayList<>();
        for (Map.Entry<String, CategoriaStats> entry : categoriaStatsMap.entrySet()) {
            CategoriaStats cs = entry.getValue();
            BigDecimal percentual = faturamentoTotal.compareTo(BigDecimal.ZERO) > 0
                ? cs.faturamento().multiply(BigDecimal.valueOf(100)).divide(faturamentoTotal, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            porCategoria.add(new RelatorioVendasResponse.VendaPorCategoria(entry.getKey(), cs.quantidade(), cs.faturamento(), percentual));
        }
        porCategoria.sort((a, b) -> b.faturamento().compareTo(a.faturamento()));

        List<RelatorioVendasResponse.MaisVendido> maisVendidos = maisVendidosMap.values().stream()
            .map(m -> new RelatorioVendasResponse.MaisVendido(m.produto(), m.categoria(), m.quantidade(), m.faturamento()))
            .sorted((a, b) -> b.quantidade() != a.quantidade() ? Long.compare(b.quantidade(), a.quantidade()) : b.faturamento().compareTo(a.faturamento()))
            .limit(10)
            .toList();

        List<RelatorioVendasResponse.VendaPorHorario> porHorario = new ArrayList<>();
        for (Map.Entry<Integer, HorarioStats> entry : porHorarioMap.entrySet()) {
            HorarioStats hs = entry.getValue();
            porHorario.add(new RelatorioVendasResponse.VendaPorHorario(entry.getKey(), hs.pedidos(), hs.faturamento()));
        }

        RelatorioVendasResponse.ResumoVendas resumo = new RelatorioVendasResponse.ResumoVendas(
            faturamentoTotal,
            totalPedidosValidos,
            ticketMedio,
            cancelados
        );

        return new RelatorioVendasResponse(resumo, porDia, porCategoria, maisVendidos, porHorario);
    }

    private LocalDateTime dataEmBrasilia(LocalDateTime dataCriacao) {
        return dataCriacao.atZone(ZoneOffset.UTC).withZoneSameInstant(ZONA_BRASILIA).toLocalDateTime();
    }

    static String normalizarStatus(String status) {
        if (status == null) return "";
        return Normalizer.normalize(status.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
    }

    static boolean statusCancelado(String status) {
        return "cancelado".equals(normalizarStatus(status));
    }

    static boolean statusConcluido(String status) {
        String normalizado = normalizarStatus(status);
        return "pedido concluido".equals(normalizado) || "concluido".equals(normalizado)
            || "entregue".equals(normalizado) || "pedido entregue".equals(normalizado);
    }

    private LocalDate validarData(String dataStr, String mensagemErro) {
        if (dataStr == null || dataStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagemErro);
        }
        try {
            return LocalDate.parse(dataStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagemErro);
        }
    }

    private void adicionarCategoria(Map<String, CategoriaStats> map, String categoria, long qtd, BigDecimal valor) {
        CategoriaStats atual = map.getOrDefault(categoria, new CategoriaStats(0, BigDecimal.ZERO));
        map.put(categoria, new CategoriaStats(atual.quantidade() + qtd, atual.faturamento().add(valor)));
    }

    private void adicionarMaisVendido(Map<String, ProdutoStats> map, String produto, String categoria, long qtd, BigDecimal valor) {
        ProdutoStats atual = map.getOrDefault(produto, new ProdutoStats(produto, categoria, 0, BigDecimal.ZERO));
        map.put(produto, new ProdutoStats(produto, categoria, atual.quantidade() + qtd, atual.faturamento().add(valor)));
    }

    private record CategoriaStats(long quantidade, BigDecimal faturamento) {}
    private record ProdutoStats(String produto, String categoria, long quantidade, BigDecimal faturamento) {}
    private record HorarioStats(long pedidos, BigDecimal faturamento) {}
}
