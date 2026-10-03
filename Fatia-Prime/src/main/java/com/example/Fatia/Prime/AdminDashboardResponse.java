package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record AdminDashboardResponse(
    long totalPedidos,
    long pedidosRecebidos,
    long pedidosEmAndamento,
    long pedidosConcluidos,
    long produtosAtivos,
    long produtosCadastrados,
    BigDecimal valorTotalPedidos
) {
}
