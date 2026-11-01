package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioVendasResponse(
    ResumoVendas resumo,
    List<VendaPorDia> porDia,
    List<VendaPorCategoria> porCategoria,
    List<MaisVendido> maisVendidos,
    List<VendaPorHorario> porHorario
) {
    public record ResumoVendas(
        BigDecimal faturamento,
        long totalPedidos,
        BigDecimal ticketMedio,
        long cancelados
    ) {}

    public record VendaPorDia(
        String data,
        BigDecimal faturamento,
        long pedidos
    ) {}

    public record VendaPorCategoria(
        String categoria,
        long quantidade,
        BigDecimal faturamento,
        BigDecimal percentual
    ) {}

    public record MaisVendido(
        String produto,
        String categoria,
        long quantidade,
        BigDecimal faturamento
    ) {}

    public record VendaPorHorario(
        int hora,
        long pedidos,
        BigDecimal faturamento
    ) {}
}
