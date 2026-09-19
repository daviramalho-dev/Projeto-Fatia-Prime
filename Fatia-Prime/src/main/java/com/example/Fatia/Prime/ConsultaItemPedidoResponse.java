package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record ConsultaItemPedidoResponse(
    Long id,
    Long produtoId,
    String nomeProduto,
    Integer quantidade,
    BigDecimal precoUnitario,
    BigDecimal subtotal
) {
    public static ConsultaItemPedidoResponse de(ItemPedido item) {
        BigDecimal subtotal = item.getPrecoUnitario()
            .multiply(BigDecimal.valueOf(item.getQuantidade()));
        return new ConsultaItemPedidoResponse(
            item.getId(),
            item.getProduto() != null ? item.getProduto().getId() : null,
            item.getProduto() != null ? item.getProduto().getNome() : null,
            item.getQuantidade(),
            item.getPrecoUnitario(),
            subtotal
        );
    }
}